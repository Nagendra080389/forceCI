package com.backgroundworker.quartzJob;

import com.commonsRabbitMQ.RabbitMqSenderConfig;
import com.commonsRabbitMQ.ScheduledRabbitMQConsumer;
import com.commonsRabbitMQ.ScheduledRabbitMQHandler;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

@Component
public class SchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);
    public static final String SCHEDULED_QUEUE_NAME = "Connect2DeployScheduledJob";
    public static final String SCHEDULED_JOB_BINDING = "Connect2DeployScheduledJobBinding";
    public static final String SCHEDULED_JOB_EXCHANGE = "Connect2DeployScheduledJobExchange";

    @Autowired
    private ScheduledJobRepositoryCustomImpl scheduledJobRepositoryCustom;
    @Autowired
    private RabbitMqSenderConfig rabbitMqSenderConfig;
    @Autowired
    private AmqpTemplate rabbitTemplateCustomAdmin;
    @Autowired
    private SFDCScheduledConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository;
    @Autowired
    private ScheduledDeploymentMongoRepository scheduledDeploymentMongoRepository;
    @Autowired
    private ScheduledLinkedServicesMongoRepository scheduledLinkedServicesMongoRepository;

    @Scheduled(fixedRate = 10000)
    void enableScheduledJob(){
        DateTime dateTime = DateTime.now(DateTimeZone.UTC);
        DateTime toDate = dateTime.plusSeconds(10);
        DateTime fromDate = dateTime.minusSeconds(10);
        List<ScheduledDeploymentJob> byStartTimeIsBetween =
                scheduledJobRepositoryCustom.findByStartTimeRunBetweenAndExecutedAndBoolActive(fromDate, toDate, false, true);


        if(byStartTimeIsBetween != null && !byStartTimeIsBetween.isEmpty()){

            try {
                Properties queueProperties = rabbitMqSenderConfig.amqpAdmin().getQueueProperties(SCHEDULED_QUEUE_NAME);
                logger.info("queueProperties -> "+queueProperties);
                if(queueProperties == null) {
                    Queue queue = new Queue(SCHEDULED_QUEUE_NAME, true);
                    rabbitMqSenderConfig.amqpAdmin().declareQueue(queue);
                    rabbitMqSenderConfig.amqpAdmin().declareExchange(new DirectExchange(SCHEDULED_JOB_EXCHANGE));
                    rabbitMqSenderConfig.amqpAdmin().declareBinding(BindingBuilder.bind(queue).to(new DirectExchange(SCHEDULED_JOB_EXCHANGE)).withQueueName());
                }
                if(queueProperties != null) {
                    ScheduledRabbitMQConsumer container = new ScheduledRabbitMQConsumer();
                    container.setConnectionFactory(rabbitMqSenderConfig.connectionFactory());
                    container.setQueueNames(SCHEDULED_QUEUE_NAME);
                    container.setMessageListener(new MessageListenerAdapter(
                            new ScheduledRabbitMQHandler(scheduledJobRepositoryCustom,
                                    sfdcConnectionDetailsMongoRepository, scheduledDeploymentMongoRepository,
                                    scheduledLinkedServicesMongoRepository),
                            new Jackson2JsonMessageConverter()));
                    logger.info("Started Consumer called from saveSfdcConnectionDetails");
                    container.startConsumers();
                    for (ScheduledDeploymentJob scheduledDeploymentJob : byStartTimeIsBetween) {
                        logger.info("Send to RabbitMQ -> " + scheduledDeploymentJob.getGitRepoId());
                        rabbitTemplateCustomAdmin.convertAndSend(SCHEDULED_JOB_EXCHANGE, SCHEDULED_QUEUE_NAME, scheduledDeploymentJob);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
