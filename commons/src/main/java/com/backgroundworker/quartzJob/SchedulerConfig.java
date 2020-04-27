package com.backgroundworker.quartzJob;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class SchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);

    @Autowired
    private ScheduledJobRepositoryCustomImpl scheduledJobRepositoryCustom;

    @Scheduled(fixedRate = 10000)
    void enableScheduledJob(){
        DateTime dateTime = DateTime.now(DateTimeZone.UTC);
        DateTime toDate = dateTime.plusSeconds(10);
        DateTime fromDate = dateTime.minusSeconds(10);
        logger.info("Poll Mongo DB from "+fromDate+ " to "+ toDate);
        List<ScheduledDeploymentJob> byStartTimeIsBetween =
                scheduledJobRepositoryCustom.findByStartTimeRunBetweenAndExecutedAndBoolActive(fromDate, toDate, false, true);
        if(byStartTimeIsBetween != null && !byStartTimeIsBetween.isEmpty()){
            logger.info("Send to RabbitMQ -> "+byStartTimeIsBetween.get(0).getGitRepoId());
            logger.info("Send to RabbitMQ -> "+byStartTimeIsBetween.get(0).getSourceBranch());
            logger.info("Send to RabbitMQ -> "+byStartTimeIsBetween.get(0).getTargetBranch());
        }
    }
}
