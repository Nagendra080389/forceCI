package com.rabbitMQ;

import com.model.SFDCConnectionDetails;
import com.webSocket.SocketHandler;
import org.springframework.data.annotation.Id;
import org.springframework.web.socket.WebSocketSession;

import java.io.Serializable;

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
    private boolean boolCompleted;
    private boolean boolSfdcValidationSuccess;
    private boolean boolCodeReviewValidationSuccess;

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

    public boolean isBoolCompleted() {
        return boolCompleted;
    }

    public void setBoolCompleted(boolean boolCompleted) {
        this.boolCompleted = boolCompleted;
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
}
