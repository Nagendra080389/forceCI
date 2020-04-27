package com.backgroundworker.quartzJob;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.Optional;

public interface DeploymentMongoRepository  extends MongoRepository<ScheduledDeploymentJob, String> {
    Optional<ScheduledDeploymentJob> findByStartTimeRunIsBetweenAndExecutedAndBoolActive(Date fromTime, Date toTime, Boolean executed, Boolean boolActive);
}
