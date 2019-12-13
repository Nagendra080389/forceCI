package com.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document(collection = "SFDCConnectionDetails")
public class SFDCConnectionDetails implements Serializable {

    private String orgName;
    private String environment;
    private String userName;
    private String instanceURL;
    private String authorize;
    private String save;
    private String testConnection;
    private String delete;
    private String oauthSuccess;
    private String oauthFailed;
    private String oauthSaved;
    private String oauthToken;

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getInstanceURL() {
        return instanceURL;
    }

    public void setInstanceURL(String instanceURL) {
        this.instanceURL = instanceURL;
    }

    public String getAuthorize() {
        return authorize;
    }

    public void setAuthorize(String authorize) {
        this.authorize = authorize;
    }

    public String getSave() {
        return save;
    }

    public void setSave(String save) {
        this.save = save;
    }

    public String getTestConnection() {
        return testConnection;
    }

    public void setTestConnection(String testConnection) {
        this.testConnection = testConnection;
    }

    public String getDelete() {
        return delete;
    }

    public void setDelete(String delete) {
        this.delete = delete;
    }

    public String getOauthSuccess() {
        return oauthSuccess;
    }

    public void setOauthSuccess(String oauthSuccess) {
        this.oauthSuccess = oauthSuccess;
    }

    public String getOauthFailed() {
        return oauthFailed;
    }

    public void setOauthFailed(String oauthFailed) {
        this.oauthFailed = oauthFailed;
    }

    public String getOauthSaved() {
        return oauthSaved;
    }

    public void setOauthSaved(String oauthSaved) {
        this.oauthSaved = oauthSaved;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(String oauthToken) {
        this.oauthToken = oauthToken;
    }
}
