package com.dao;

import com.rabbitMQ.DeploymentJob;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DeploymentJobMongoRepository extends MongoRepository<DeploymentJob, String> {

    Long countByTargetBranch(String targetBranch);

    Long countByRepoId(String repoId);

    List<DeploymentJob> findByTargetBranch(String targetBranch);

    List<DeploymentJob> findByRepoId(String repoId);

    List<DeploymentJob> findByRepoIdAndTargetBranch(String repoId, String targetBranch);

    Long countByRepoIdAndTargetBranch(String repoId, String targetBranch);

    List<DeploymentJob> findByJobIdAndRepoId(String jobId, String repoId);

    List<DeploymentJob> findByRepoIdAndBaseSHAOrderByJobIdDesc(String repoId, String baseSHA);

    void deleteAllByRepoId(String repoId);

}
