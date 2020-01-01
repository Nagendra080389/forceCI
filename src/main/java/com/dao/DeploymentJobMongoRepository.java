package com.dao;

import com.rabbitMQ.DeploymentJob;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeploymentJobMongoRepository extends MongoRepository<DeploymentJob, String> {

    Long countByTargetBranch(String targetBranch);
}
