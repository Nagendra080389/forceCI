package com.dao;

import com.model.UserWrapper;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserWrapperMongoRepository extends MongoRepository<UserWrapper, Integer> {

    UserWrapper findByOwnerId(String ownerId);
}
