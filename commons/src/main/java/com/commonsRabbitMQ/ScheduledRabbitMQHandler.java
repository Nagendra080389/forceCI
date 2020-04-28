package com.commonsRabbitMQ;

import com.backgroundworker.quartzJob.SFDCScheduledConnectionDetailsMongoRepository;
import com.backgroundworker.quartzJob.ScheduledDeploymentJob;
import com.backgroundworker.quartzJob.ScheduledJobRepositoryCustomImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledRabbitMQHandler {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledRabbitMQHandler.class);

    public ScheduledRabbitMQHandler(ScheduledJobRepositoryCustomImpl scheduledJobRepositoryCustom,
                                    SFDCScheduledConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository) {

    }

    public void handleMessage(ScheduledDeploymentJob scheduledDeploymentJob) {

    }
}
