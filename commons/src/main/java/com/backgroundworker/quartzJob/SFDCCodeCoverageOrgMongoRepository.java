package com.backgroundworker.quartzJob;

import com.codecoverage.SFDCCodeCoverageOrg;
import com.model.LinkedServices;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SFDCCodeCoverageOrgMongoRepository extends MongoRepository<SFDCCodeCoverageOrg, String> {

    Optional<SFDCCodeCoverageOrg> findByScheduledJobId(String scheduledJobId);
}
