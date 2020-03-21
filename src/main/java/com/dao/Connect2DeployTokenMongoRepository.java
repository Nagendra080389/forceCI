package com.dao;

import com.model.Connect2DeployToken;
import com.model.Connect2DeployUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface Connect2DeployTokenMongoRepository extends MongoRepository<Connect2DeployToken, String> {

    Connect2DeployToken findByConfirmationToken(String confirmationToken);

}
