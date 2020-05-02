package com.commonsRabbitMQ;

import com.backgroundworker.quartzJob.*;
import com.model.LinkedServices;
import com.model.SFDCConnectionDetails;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import static com.backgroundworker.quartzJob.SchedulerConfig.SCHEDULED_QUEUE_NAME;

@Component
public class ScheduledRabbitMQHandler {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledRabbitMQHandler.class);
    public static final String GIT_HUB = "GitHub";
    public static final String GIT_HUB_ENTERPRISE = "GitHub Enterprise";

    @Autowired
    private SFDCScheduledConnectionDetailsMongoRepository sfdcConnectionDetailsMongoRepository;
    @Autowired
    private ScheduledDeploymentMongoRepository scheduledDeploymentMongoRepository;
    @Autowired
    private ScheduledLinkedServicesMongoRepository scheduledLinkedServicesMongoRepository;

    @RabbitListener(queues=SCHEDULED_QUEUE_NAME)
    public void receivedMessage(ScheduledDeploymentJob scheduledDeploymentJob) {
        try {
            logger.info("scheduledDeploymentJob -> " + scheduledDeploymentJob.getGitRepoId());
            scheduledDeploymentJob.setExecuted(true);
            scheduledDeploymentJob.setStatus(Status.RUNNING.getText());
            scheduledDeploymentJob.setLastTimeRun(scheduledDeploymentJob.getStartTimeRun());
            scheduledDeploymentMongoRepository.save(scheduledDeploymentJob);
            Optional<SFDCConnectionDetails> objSfdcConnectionDetails = sfdcConnectionDetailsMongoRepository.findById(scheduledDeploymentJob.getSfdcConnection());
            if (objSfdcConnectionDetails.isPresent()) {
                SFDCConnectionDetails sfdcConnectionDetails = objSfdcConnectionDetails.get();
                String gitRepoId = sfdcConnectionDetails.getGitRepoId();
                String linkedService = sfdcConnectionDetails.getLinkedService();
                String connect2DeployUser = sfdcConnectionDetails.getConnect2DeployUser();
                List<LinkedServices> linkedServiceByUserName = scheduledLinkedServicesMongoRepository.findByConnect2DeployUser(connect2DeployUser);
                GitHub gitHub = null;
                if (linkedServiceByUserName != null && !linkedServiceByUserName.isEmpty()) {
                    for (LinkedServices eachService : linkedServiceByUserName) {
                        if (linkedService.equalsIgnoreCase(eachService.getName()) && linkedService.equalsIgnoreCase(GIT_HUB)) {
                            gitHub = GitHub.connectUsingOAuth(eachService.getAccessToken());
                        } else if (linkedService.equalsIgnoreCase(eachService.getName()) && linkedService.equalsIgnoreCase(GIT_HUB_ENTERPRISE)) {
                            gitHub = GitHub.connectToEnterpriseWithOAuth(eachService.getServerURL(), eachService.getUserEmail(), eachService.getAccessToken());
                        }
                    }
                }

                GHRepository trailHeadGitRepo = gitHub.getRepositoryById(gitRepoId);
                String sourceBranch = trailHeadGitRepo.getBranch(scheduledDeploymentJob.getSourceBranch()).getSHA1();
                GHRef ref = trailHeadGitRepo.createRef("refs/heads/" + scheduledDeploymentJob.getJobName(), sourceBranch);
                trailHeadGitRepo.createPullRequest(scheduledDeploymentJob.getJobName(), ref.getRef(), scheduledDeploymentJob.getTargetBranch(), scheduledDeploymentJob.getJobName());
                scheduledDeploymentJob.setStatus(Status.FINISHED.getText());
                scheduledDeploymentMongoRepository.save(scheduledDeploymentJob);
                logger.info("Pull Request Successfully created");
            }
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }
    }
}
