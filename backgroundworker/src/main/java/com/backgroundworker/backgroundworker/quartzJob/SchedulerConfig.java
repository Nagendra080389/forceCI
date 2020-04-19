package com.backgroundworker.backgroundworker.quartzJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);

    @Scheduled(fixedRate = 10000)
    void enableScheduledJob(){
        logger.info("Poll Mongo DB");
    }
}
