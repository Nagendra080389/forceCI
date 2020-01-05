package com.rabbitMQ;

import com.model.SFDCConnectionDetails;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class DeploymentJob implements Serializable {

    @Id
    private String id;
    private String jobId;
    private String access_token;
    private String emailId;
    private String userName;
    private String gitCloneURL;
    private String sourceBranch;
    private String targetBranch;
    private SFDCConnectionDetails sfdcConnectionDetail;
    private String queueName;
    private List<String> lstBuildLines;
    private boolean boolSFDCCompleted;
    private boolean boolCodeScanCompleted;
    private boolean boolSfdcValidationSuccess;
    private boolean boolCodeReviewValidationSuccess;
    @CreatedDate
    private Date createdDate;
    @LastModifiedDate
    private Date lastModifiedDate;

    public String getId() {
        return id;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGitCloneURL() {
        return gitCloneURL;
    }

    public void setGitCloneURL(String gitCloneURL) {
        this.gitCloneURL = gitCloneURL;
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

    public SFDCConnectionDetails getSfdcConnectionDetail() {
        return sfdcConnectionDetail;
    }

    public void setSfdcConnectionDetail(SFDCConnectionDetails sfdcConnectionDetail) {
        this.sfdcConnectionDetail = sfdcConnectionDetail;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public boolean isBoolSFDCCompleted() {
        return boolSFDCCompleted;
    }

    public void setBoolSFDCCompleted(boolean boolSFDCCompleted) {
        this.boolSFDCCompleted = boolSFDCCompleted;
    }

    public boolean isBoolCodeScanCompleted() {
        return boolCodeScanCompleted;
    }

    public void setBoolCodeScanCompleted(boolean boolCodeScanCompleted) {
        this.boolCodeScanCompleted = boolCodeScanCompleted;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public boolean isBoolSfdcValidationSuccess() {
        return boolSfdcValidationSuccess;
    }

    public void setBoolSfdcValidationSuccess(boolean boolSfdcValidationSuccess) {
        this.boolSfdcValidationSuccess = boolSfdcValidationSuccess;
    }

    public boolean isBoolCodeReviewValidationSuccess() {
        return boolCodeReviewValidationSuccess;
    }

    public void setBoolCodeReviewValidationSuccess(boolean boolCodeReviewValidationSuccess) {
        this.boolCodeReviewValidationSuccess = boolCodeReviewValidationSuccess;
    }

    public List<String> getLstBuildLines() {
        return lstBuildLines;
    }

    public void setLstBuildLines(List<String> lstBuildLines) {
        this.lstBuildLines = lstBuildLines;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeploymentJob that = (DeploymentJob) o;
        return boolSFDCCompleted == that.boolSFDCCompleted &&
                boolCodeScanCompleted == that.boolCodeScanCompleted &&
                boolSfdcValidationSuccess == that.boolSfdcValidationSuccess &&
                boolCodeReviewValidationSuccess == that.boolCodeReviewValidationSuccess &&
                Objects.equals(id, that.id) &&
                Objects.equals(jobId, that.jobId) &&
                Objects.equals(access_token, that.access_token) &&
                Objects.equals(emailId, that.emailId) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(gitCloneURL, that.gitCloneURL) &&
                Objects.equals(sourceBranch, that.sourceBranch) &&
                Objects.equals(targetBranch, that.targetBranch) &&
                Objects.equals(sfdcConnectionDetail, that.sfdcConnectionDetail) &&
                Objects.equals(queueName, that.queueName) &&
                Objects.equals(createdDate, that.createdDate) &&
                Objects.equals(lastModifiedDate, that.lastModifiedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, jobId, access_token, emailId, userName, gitCloneURL, sourceBranch, targetBranch, sfdcConnectionDetail, queueName, boolSFDCCompleted, boolCodeScanCompleted, boolSfdcValidationSuccess, boolCodeReviewValidationSuccess, createdDate, lastModifiedDate);
    }
}


