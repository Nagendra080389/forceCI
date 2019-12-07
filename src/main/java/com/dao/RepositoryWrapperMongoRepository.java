package com.dao;

import com.model.RepositoryWrapper;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RepositoryWrapperMongoRepository extends MongoRepository<RepositoryWrapper, Integer> {

    RepositoryWrapper findByOwnerId(String ownerId);
}
