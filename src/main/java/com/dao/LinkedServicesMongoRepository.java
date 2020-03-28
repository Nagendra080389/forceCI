package com.dao;

import com.model.LinkedServices;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LinkedServicesMongoRepository extends MongoRepository<LinkedServices, String> {
    LinkedServices findByUserName(String userName);
}
