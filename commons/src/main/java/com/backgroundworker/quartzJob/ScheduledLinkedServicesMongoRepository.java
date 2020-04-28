package com.backgroundworker.quartzJob;

import com.model.LinkedServices;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ScheduledLinkedServicesMongoRepository  extends MongoRepository<LinkedServices, String> {
    LinkedServices findByUserName(String userName);

    List<LinkedServices> findByConnect2DeployUser(String connect2DeployUser);
}
