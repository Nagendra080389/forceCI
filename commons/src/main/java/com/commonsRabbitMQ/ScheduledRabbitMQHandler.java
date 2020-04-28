package com.commonsRabbitMQ;

import com.backgroundworker.quartzJob.SFDCScheduledConnectionDetailsMongoRepository;
import com.backgroundworker.quartzJob.ScheduledDeploymentJob;
import com.backgroundworker.quartzJob.ScheduledJobRepositoryCustomImpl;
import com.model.SFDCConnectionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ScheduledRabbitMQHandler {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledRabbitMQHandler.class);

    private ScheduledJobRepositoryCustomImpl scheduledJobRepositoryCustom;
    private SFDCScheduledConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository;

    public ScheduledRabbitMQHandler(ScheduledJobRepositoryCustomImpl scheduledJobRepositoryCustom,
                                    SFDCScheduledConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository) {
        this.scheduledJobRepositoryCustom = scheduledJobRepositoryCustom;
        this.sfdcConnectionDetailsMongoRepository = sfdcConnectionDetailsMongoRepository;

    }

    public void handleMessage(ScheduledDeploymentJob scheduledDeploymentJob) {
        logger.info("scheduledDeploymentJob -> "+scheduledDeploymentJob.getGitRepoId());
        List<SFDCConnectionDetails> byGitRepoId = sfdcConnectionDetailsMongoRepository.findByGitRepoId(scheduledDeploymentJob.getGitRepoId());
        if(byGitRepoId != null && !byGitRepoId.isEmpty()) {
            logger.info("byGitRepoId -> " + byGitRepoId.get(0));
        }

    }
}
