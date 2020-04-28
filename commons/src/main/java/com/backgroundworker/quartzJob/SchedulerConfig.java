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
    public static final String CONNECT_2_DEPLOY_SCHEDULED_JOB = "Connect2DeployScheduledJob";

    @Autowired
    private ScheduledJobRepositoryCustomImpl scheduledJobRepositoryCustom;
    @Autowired
    private RabbitMqSenderConfig rabbitMqSenderConfig;
    @Autowired
    private AmqpTemplate rabbitTemplateCustomAdmin;
    @Autowired
    private SFDCScheduledConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository;

    @Scheduled(fixedRate = 10000)
    void enableScheduledJob(){
        DateTime dateTime = DateTime.now(DateTimeZone.UTC);
        DateTime toDate = dateTime.plusSeconds(10);
        DateTime fromDate = dateTime.minusSeconds(10);
        List<ScheduledDeploymentJob> byStartTimeIsBetween =
                scheduledJobRepositoryCustom.findByStartTimeRunBetweenAndExecutedAndBoolActive(fromDate, toDate, false, true);
        logger.info("RabbitMQ Config -> "+rabbitMqSenderConfig);
        logger.info("RabbitMQ template -> "+rabbitTemplateCustomAdmin);
        logger.info("SFDC Connection template -> "+sfdcConnectionDetailsMongoRepository);



        if(byStartTimeIsBetween != null && !byStartTimeIsBetween.isEmpty()){

            try {
                Properties queueProperties = rabbitMqSenderConfig.amqpAdmin().getQueueProperties(CONNECT_2_DEPLOY_SCHEDULED_JOB);
                logger.info("queueProperties -> "+queueProperties);
                Queue queue = null;
                if(queueProperties == null) {
                    queue = new Queue(CONNECT_2_DEPLOY_SCHEDULED_JOB, true);
                    rabbitMqSenderConfig.amqpAdmin().declareQueue(queue);
                    rabbitMqSenderConfig.amqpAdmin().declareBinding(BindingBuilder.bind(queue).to(new DirectExchange("Connect2DeployScheduledJobExchange")).withQueueName());
                }
                ScheduledRabbitMQConsumer container = new ScheduledRabbitMQConsumer();
                container.setConnectionFactory(rabbitMqSenderConfig.connectionFactory());
                container.setQueueNames(CONNECT_2_DEPLOY_SCHEDULED_JOB);
                container.setMessageListener(new MessageListenerAdapter(new ScheduledRabbitMQHandler(scheduledJobRepositoryCustom, sfdcConnectionDetailsMongoRepository), new Jackson2JsonMessageConverter()));
                logger.info("Started Consumer called from saveSfdcConnectionDetails");
                container.startConsumers();
            } catch (Exception e) {
                e.printStackTrace();
            }

            logger.info("Send to RabbitMQ -> "+byStartTimeIsBetween.get(0).getGitRepoId());
            logger.info("Send to RabbitMQ -> "+byStartTimeIsBetween.get(0).getSourceBranch());
            logger.info("Send to RabbitMQ -> "+byStartTimeIsBetween.get(0).getTargetBranch());
        }
    }
}
