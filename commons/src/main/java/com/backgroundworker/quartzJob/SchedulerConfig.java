package com.backgroundworker.quartzJob;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
public class SchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);

    @Autowired
    private DeploymentMongoRepository deploymentMongoRepository;

    @Scheduled(fixedRate = 10000)
    void enableScheduledJob(){
        logger.info("Poll Mongo DB");
        DateTime dateTime = DateTime.now(DateTimeZone.UTC);
        Date toDate = dateTime.plusSeconds(10).toDate();
        Date fromDate = dateTime.minusSeconds(10).toDate();
        Optional<ScheduledDeploymentJob> byStartTimeIsBetween =
                deploymentMongoRepository.
                        findByStartTimeRunIsBetweenAndExecutedAndBoolActive(fromDate, toDate, false, true);
        if(byStartTimeIsBetween.isPresent()){
            logger.info("Send to RabbitMQ -> "+byStartTimeIsBetween.get().getGitRepoId());
            logger.info("Send to RabbitMQ -> "+byStartTimeIsBetween.get().getSourceBranch());
            logger.info("Send to RabbitMQ -> "+byStartTimeIsBetween.get().getTargetBranch());
        }
    }
}