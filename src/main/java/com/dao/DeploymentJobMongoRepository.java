package com.dao;

import com.rabbitMQ.DeploymentJob;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DeploymentJobMongoRepository extends MongoRepository<DeploymentJob, String> {

    Long countByTargetBranch(String targetBranch);

    Long countByRepoId(String repoId);

    List<DeploymentJob> findByTargetBranch(String targetBranch);

    List<DeploymentJob> findByRepoId(String repoId);

}
