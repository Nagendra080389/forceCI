package com.dao;

import com.model.SFDCConnectionDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SFDCConnectionDetailsMongoRepository extends MongoRepository<SFDCConnectionDetails, Integer> {

    SFDCConnectionDetails findByUserName(String UserName);

    List<SFDCConnectionDetails> findByGitRepoId(String gitRepoId);

    SFDCConnectionDetails findById(String id);
}
