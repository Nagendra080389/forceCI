package com.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document(collection = "SFDCConnectionDetails")
public class SFDCConnectionDetails implements Serializable {

    private String OrgName;
    private String Environment;
    private String UserName;
    private String InstanceURL;
    private String authorize;
    private String save;
    private String testConnection;
    private String delete;
    private String oauthSuccess;
    private String oauthFailed;
    private String oauthSaved;
    private String oauthToken;

    public String getOrgName() {
        return OrgName;
    }

    public void setOrgName(String orgName) {
        OrgName = orgName;
    }

    public String getEnvironment() {
        return Environment;
    }

    public void setEnvironment(String environment) {
        Environment = environment;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getInstanceURL() {
        return InstanceURL;
    }

    public void setInstanceURL(String instanceURL) {
        InstanceURL = instanceURL;
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
