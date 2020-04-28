package com.backgroundworker.quartzJob;

import com.model.SFDCConnectionDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SFDCConnectionDetailsMongoRepository extends MongoRepository<SFDCConnectionDetails, String> {

    SFDCConnectionDetails findByUserName(String UserName);

    List<SFDCConnectionDetails> findByGitRepoId(String gitRepoId);

    SFDCConnectionDetails findByBranchConnectedToAndBoolActive(String branchConnectedTo, boolean boolActive);

    List<SFDCConnectionDetails> findByConnect2DeployUserAndBoolActive(String connect2DeployUser, boolean boolActive);
}
