package com.dao;

import com.model.Connect2DeployToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface Connect2DeployTokenMongoRepository extends MongoRepository<Connect2DeployToken, String> {

    Connect2DeployToken findByConfirmationToken(String confirmationToken);

    Connect2DeployToken findByUserIdAndConfirmationToken(String userId, String confirmationToken);

}
