package com.dao;

import com.model.RepositoryWrapper;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RepositoryWrapperMongoRepository extends MongoRepository<RepositoryWrapper, Integer> {

    List<RepositoryWrapper> findByOwnerId(String ownerId);

    RepositoryWrapper findByRepositoryRepositoryId (String repositoryId);

    RepositoryWrapper findByOwnerIdAndRepositoryRepositoryId (String ownerId, String repositoryId);
}
