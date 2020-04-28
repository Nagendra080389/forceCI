package com.backgroundworker.quartzJob;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface DeploymentMongoRepository  extends ScheduledJobRepositoryCustom, MongoRepository<ScheduledDeploymentJob, String> {

    Optional<List<ScheduledDeploymentJob>> findByConnect2DeployUserEmail(String connect2DeployUserEmail);

}
