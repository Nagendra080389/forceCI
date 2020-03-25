package com.dao;

import com.model.ConnectionDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ConnectionDetailsMongoRepository extends MongoRepository<ConnectionDetails, String> {

    Optional<ConnectionDetails> findByUui(String Uui);

}
