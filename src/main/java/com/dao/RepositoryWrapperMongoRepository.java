package com.dao;

import com.model.RepositoryWrapper;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RepositoryWrapperMongoRepository extends MongoRepository<RepositoryWrapper, Integer> {

    List<RepositoryWrapper> findByOwnerId(String ownerId);

    RepositoryWrapper findByRepositoryRepositoryName (String repositoryName);

    RepositoryWrapper findByOwnerIdAndRepositoryRepositoryName (String ownerId, String repositoryName);
}
