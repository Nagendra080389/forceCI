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
    public static final String SCHEDULED_JOB_EXCHANGE = "Connect2DeployScheduledJobExchange";
    public static final String TESTING_JOB = "TestingJob";
    public static final String DEPLOYMENT_JOB = "DeploymentJob";

    @Autowired
    private ScheduledJobRepositoryCustomImpl scheduledJobRepositoryCustom;
    @Autowired
    private AmqpTemplate rabbitTemplateCustomAdmin;

    @Scheduled(fixedRate = 10000)
    void enableScheduledJob(){
        DateTime dateTime = DateTime.now(DateTimeZone.UTC);
        DateTime toDate = dateTime.plusSeconds(10);
        DateTime fromDate = dateTime.minusSeconds(10);
        List<ScheduledDeploymentJob> deploymentJobs = scheduledJobRepositoryCustom.findByStartTimeRunBetweenAndExecutedAndBoolActive(fromDate, toDate, false, true, DEPLOYMENT_JOB);
        List<ScheduledDeploymentJob> testingJobs = scheduledJobRepositoryCustom.findByStartTimeRunBetweenAndExecutedAndBoolActive(fromDate, toDate, false, true, TESTING_JOB);


        if(deploymentJobs != null && !deploymentJobs.isEmpty()){
            for (ScheduledDeploymentJob scheduledDeploymentJob : deploymentJobs) {
                logger.info("Send to RabbitMQ -> " + scheduledDeploymentJob.getGitRepoId());
                rabbitTemplateCustomAdmin.convertAndSend(SCHEDULED_JOB_EXCHANGE, SCHEDULED_QUEUE_NAME, scheduledDeploymentJob);
            }

        }
    }
}
