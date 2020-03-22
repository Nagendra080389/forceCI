package com.dao;

import com.model.Connect2DeployUser;
import com.rabbitMQ.DeploymentJob;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface Connect2DeployUserMongoRepository extends MongoRepository<Connect2DeployUser, String> {

    Connect2DeployUser findByEmailId(String emailId);

    Optional<Connect2DeployUser> findByToken(String token);

}
