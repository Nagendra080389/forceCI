package com.backgroundworker.quartzJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ScheduledJobRepositoryCustomImpl implements ScheduledJobRepositoryCustom{

    private static final Logger logger = LoggerFactory.getLogger(ScheduledJobRepositoryCustomImpl.class);
    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<ScheduledDeploymentJob> findByStartTimeRunBetweenAndExecutedAndBoolActive(Date from, Date to, Boolean executed, Boolean boolActive) {
        Query query = new Query();
        query.addCriteria(
                new Criteria().andOperator(
                        Criteria.where("startTimeRun").gte(from).lte(to),
                        Criteria.where("boolActive").is(boolActive),
                        Criteria.where("executed").is(executed)
                )
        );
        logger.info("Query Generated -> "+query.toString());
        return mongoTemplate.find(query, ScheduledDeploymentJob.class);
    }
}
