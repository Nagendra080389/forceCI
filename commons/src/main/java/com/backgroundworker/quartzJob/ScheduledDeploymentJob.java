package com.backgroundworker.quartzJob;

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
    private String jobName;
    private String createdBy;
    private String connect2DeployUserEmail;
    private String orgUserEmail;
    private String status;
    private String sfdcConnection;
    private Date startTimeRun;
    private Date lastTimeRun;
    private Date nextTimeRun;
    private String cronExpression;
    private Boolean executed;
    private Boolean boolActive;

    public String getId() {
        return id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getLastTimeRun() {
        return lastTimeRun;
    }

    public void setLastTimeRun(Date lastTimeRun) {
        this.lastTimeRun = lastTimeRun;
    }

    public String getGitRepoId() {
        return gitRepoId;
    }

    public void setGitRepoId(String gitRepoId) {
        this.gitRepoId = gitRepoId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getConnect2DeployUserEmail() {
        return connect2DeployUserEmail;
    }

    public String getSfdcConnection() {
        return sfdcConnection;
    }

    public void setSfdcConnection(String sfdcConnection) {
        this.sfdcConnection = sfdcConnection;
    }

    public String getOrgUserEmail() {
        return orgUserEmail;
    }

    public void setOrgUserEmail(String orgUserEmail) {
        this.orgUserEmail = orgUserEmail;
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

    public Boolean getBoolActive() {
        return boolActive;
    }

    public void setBoolActive(Boolean boolActive) {
        this.boolActive = boolActive;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
                Objects.equals(jobName, that.jobName) &&
                Objects.equals(connect2DeployUserEmail, that.connect2DeployUserEmail) &&
                Objects.equals(startTimeRun, that.startTimeRun) &&
                Objects.equals(nextTimeRun, that.nextTimeRun) &&
                Objects.equals(cronExpression, that.cronExpression) &&
                Objects.equals(executed, that.executed) &&
                Objects.equals(boolActive, that.boolActive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sourceBranch, targetBranch, gitRepoId, jobName, connect2DeployUserEmail, startTimeRun, nextTimeRun, cronExpression, executed, boolActive);
    }
}
