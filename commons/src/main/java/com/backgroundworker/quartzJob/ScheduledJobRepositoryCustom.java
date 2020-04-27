package com.backgroundworker.quartzJob;

import java.util.Date;
import java.util.List;

public interface ScheduledJobRepositoryCustom {

    public List<ScheduledDeploymentJob> findByStartTimeRunBetweenAndExecutedAndBoolActive(Date from, Date to, Boolean executed, Boolean boolActive);
}
