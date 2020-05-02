package com.backgroundworker.quartzJob;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

public interface ScheduledJobRepositoryCustom {

    public List<ScheduledDeploymentJob> findByStartTimeRunBetweenAndExecutedAndBoolActive(DateTime from, DateTime to, Boolean executed, Boolean boolActive, String jobType);
}
