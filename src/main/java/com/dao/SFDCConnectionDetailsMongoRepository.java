package com.dao;

import com.model.SFDCConnectionDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SFDCConnectionDetailsMongoRepository extends MongoRepository<SFDCConnectionDetails, Integer> {

    SFDCConnectionDetails findByUserName(String UserName);
}
