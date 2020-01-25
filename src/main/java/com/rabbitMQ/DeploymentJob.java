package com.rabbitMQ;

import com.model.SFDCConnectionDetails;
import com.pmd.PMDStructure;
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
    private String repoId;
    private String baseSHA;
    private List<String> lstBuildLines;
    private List<String> lstDeploymentBuildLines;
    private boolean boolSfdcCompleted;
    private boolean boolSfdcRunning;
    private boolean boolSfdcPass;
    private boolean boolSfdcFail;
    private boolean boolSfdcDeploymentCompleted;
    private boolean boolSfdcDeploymentRunning;
    private boolean boolSfdcDeploymentPass;
    private boolean boolSfdcDeploymentFail;
    private boolean boolSfdcDeploymentNotStarted;
    private boolean boolCodeReviewCompleted;
    private boolean boolCodeReviewRunning;
    private boolean boolCodeReviewPass;
    private boolean boolCodeReviewFail;
    private boolean boolMerge;
    private boolean boolCodeReviewNotStarted;
    private String pullRequestNumber;
    private String pullRequestTitle;
    private String pullRequestHtmlUrl;
    @CreatedDate
    private Date createdDate;
    @LastModifiedDate
    private Date lastModifiedDate;
    private List<PMDStructure> lstPmdStructures;

    public boolean isBoolCodeReviewNotStarted() {
        return boolCodeReviewNotStarted;
    }

    public void setBoolCodeReviewNotStarted(boolean boolCodeReviewNotStarted) {
        this.boolCodeReviewNotStarted = boolCodeReviewNotStarted;
    }

    public String getBaseSHA() {
        return baseSHA;
    }

    public void setBaseSHA(String baseSHA) {
        this.baseSHA = baseSHA;
    }

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

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public List<String> getLstDeploymentBuildLines() {
        return lstDeploymentBuildLines;
    }

    public void setLstDeploymentBuildLines(List<String> lstDeploymentBuildLines) {
        this.lstDeploymentBuildLines = lstDeploymentBuildLines;
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

    public boolean isBoolSfdcCompleted() {
        return boolSfdcCompleted;
    }

    public void setBoolSfdcCompleted(boolean boolSfdcCompleted) {
        this.boolSfdcCompleted = boolSfdcCompleted;
    }

    public boolean isBoolSfdcRunning() {
        return boolSfdcRunning;
    }

    public void setBoolSfdcRunning(boolean boolSfdcRunning) {
        this.boolSfdcRunning = boolSfdcRunning;
    }

    public boolean isBoolSfdcPass() {
        return boolSfdcPass;
    }

    public void setBoolSfdcPass(boolean boolSfdcPass) {
        this.boolSfdcPass = boolSfdcPass;
    }

    public boolean isBoolSfdcFail() {
        return boolSfdcFail;
    }

    public void setBoolSfdcFail(boolean boolSfdcFail) {
        this.boolSfdcFail = boolSfdcFail;
    }

    public boolean isBoolCodeReviewCompleted() {
        return boolCodeReviewCompleted;
    }

    public void setBoolCodeReviewCompleted(boolean boolCodeReviewCompleted) {
        this.boolCodeReviewCompleted = boolCodeReviewCompleted;
    }

    public boolean isBoolCodeReviewRunning() {
        return boolCodeReviewRunning;
    }

    public void setBoolCodeReviewRunning(boolean boolCodeReviewRunning) {
        this.boolCodeReviewRunning = boolCodeReviewRunning;
    }

    public boolean isBoolCodeReviewPass() {
        return boolCodeReviewPass;
    }

    public void setBoolCodeReviewPass(boolean boolCodeReviewPass) {
        this.boolCodeReviewPass = boolCodeReviewPass;
    }

    public boolean isBoolCodeReviewFail() {
        return boolCodeReviewFail;
    }

    public void setBoolCodeReviewFail(boolean boolCodeReviewFail) {
        this.boolCodeReviewFail = boolCodeReviewFail;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getPullRequestNumber() {
        return pullRequestNumber;
    }

    public void setPullRequestNumber(String pullRequestNumber) {
        this.pullRequestNumber = pullRequestNumber;
    }

    public String getPullRequestTitle() {
        return pullRequestTitle;
    }

    public void setPullRequestTitle(String pullRequestTitle) {
        this.pullRequestTitle = pullRequestTitle;
    }

    public String getPullRequestHtmlUrl() {
        return pullRequestHtmlUrl;
    }

    public void setPullRequestHtmlUrl(String pullRequestHtmlUrl) {
        this.pullRequestHtmlUrl = pullRequestHtmlUrl;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public List<PMDStructure> getLstPmdStructures() {
        return lstPmdStructures;
    }

    public void setLstPmdStructures(List<PMDStructure> lstPmdStructures) {
        this.lstPmdStructures = lstPmdStructures;
    }

    public boolean isBoolMerge() {
        return boolMerge;
    }

    public void setBoolMerge(boolean boolMerge) {
        this.boolMerge = boolMerge;
    }

    public boolean isBoolSfdcDeploymentCompleted() {
        return boolSfdcDeploymentCompleted;
    }

    public void setBoolSfdcDeploymentCompleted(boolean boolSfdcDeploymentCompleted) {
        this.boolSfdcDeploymentCompleted = boolSfdcDeploymentCompleted;
    }

    public boolean isBoolSfdcDeploymentRunning() {
        return boolSfdcDeploymentRunning;
    }

    public void setBoolSfdcDeploymentRunning(boolean boolSfdcDeploymentRunning) {
        this.boolSfdcDeploymentRunning = boolSfdcDeploymentRunning;
    }

    public boolean isBoolSfdcDeploymentPass() {
        return boolSfdcDeploymentPass;
    }

    public void setBoolSfdcDeploymentPass(boolean boolSfdcDeploymentPass) {
        this.boolSfdcDeploymentPass = boolSfdcDeploymentPass;
    }

    public boolean isBoolSfdcDeploymentFail() {
        return boolSfdcDeploymentFail;
    }

    public void setBoolSfdcDeploymentFail(boolean boolSfdcDeploymentFail) {
        this.boolSfdcDeploymentFail = boolSfdcDeploymentFail;
    }

    public boolean isBoolSfdcDeploymentNotStarted() {
        return boolSfdcDeploymentNotStarted;
    }

    public void setBoolSfdcDeploymentNotStarted(boolean boolSfdcDeploymentNotStarted) {
        this.boolSfdcDeploymentNotStarted = boolSfdcDeploymentNotStarted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeploymentJob that = (DeploymentJob) o;
        return boolSfdcCompleted == that.boolSfdcCompleted &&
                boolSfdcRunning == that.boolSfdcRunning &&
                boolSfdcPass == that.boolSfdcPass &&
                boolSfdcFail == that.boolSfdcFail &&
                boolCodeReviewCompleted == that.boolCodeReviewCompleted &&
                boolCodeReviewRunning == that.boolCodeReviewRunning &&
                boolCodeReviewPass == that.boolCodeReviewPass &&
                boolCodeReviewFail == that.boolCodeReviewFail &&
                id.equals(that.id) &&
                jobId.equals(that.jobId) &&
                Objects.equals(access_token, that.access_token) &&
                Objects.equals(emailId, that.emailId) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(gitCloneURL, that.gitCloneURL) &&
                Objects.equals(sourceBranch, that.sourceBranch) &&
                Objects.equals(targetBranch, that.targetBranch) &&
                Objects.equals(sfdcConnectionDetail, that.sfdcConnectionDetail) &&
                Objects.equals(queueName, that.queueName) &&
                Objects.equals(lstBuildLines, that.lstBuildLines) &&
                Objects.equals(createdDate, that.createdDate) &&
                Objects.equals(lastModifiedDate, that.lastModifiedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, jobId, access_token, emailId, userName, gitCloneURL, sourceBranch, targetBranch, sfdcConnectionDetail, queueName, lstBuildLines, boolSfdcCompleted, boolSfdcRunning, boolSfdcPass, boolSfdcFail, boolCodeReviewCompleted, boolCodeReviewRunning, boolCodeReviewPass, boolCodeReviewFail, createdDate, lastModifiedDate);
    }
}


