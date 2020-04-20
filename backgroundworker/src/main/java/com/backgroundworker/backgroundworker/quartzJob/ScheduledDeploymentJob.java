package com.backgroundworker.backgroundworker.quartzJob;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class ScheduledDeploymentJob implements Serializable {

    @Id
    private String id;
    private String sourceBranch;
    private String targetBranch;
    private String gitRepoId;
    private String connect2DeployUserEmail;
    private Date startTimeRun;
    private Date nextTimeRun;
    private String cronExpression;
    private Boolean executed;

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    public String getGitRepoId() {
        return gitRepoId;
    }

    public void setGitRepoId(String gitRepoId) {
        this.gitRepoId = gitRepoId;
    }

    public String getConnect2DeployUserEmail() {
        return connect2DeployUserEmail;
    }

    public void setConnect2DeployUserEmail(String connect2DeployUserEmail) {
        this.connect2DeployUserEmail = connect2DeployUserEmail;
    }

    public Date getStartTimeRun() {
        return startTimeRun;
    }

    public void setStartTimeRun(Date startTimeRun) {
        this.startTimeRun = startTimeRun;
    }

    public Date getNextTimeRun() {
        return nextTimeRun;
    }

    public void setNextTimeRun(Date nextTimeRun) {
        this.nextTimeRun = nextTimeRun;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Boolean getExecuted() {
        return executed;
    }

    public void setExecuted(Boolean executed) {
        this.executed = executed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduledDeploymentJob that = (ScheduledDeploymentJob) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(sourceBranch, that.sourceBranch) &&
                Objects.equals(targetBranch, that.targetBranch) &&
                Objects.equals(gitRepoId, that.gitRepoId) &&
                Objects.equals(connect2DeployUserEmail, that.connect2DeployUserEmail) &&
                Objects.equals(startTimeRun, that.startTimeRun) &&
                Objects.equals(nextTimeRun, that.nextTimeRun) &&
                Objects.equals(cronExpression, that.cronExpression) &&
                Objects.equals(executed, that.executed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sourceBranch, targetBranch, gitRepoId, connect2DeployUserEmail, startTimeRun, nextTimeRun, cronExpression, executed);
    }
}
