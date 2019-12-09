package com.model;

import java.io.Serializable;
import java.util.List;

public class Repository implements Serializable {
    private String repositoryId;
    private String repositoryURL;
    private String repositoryOwnerAvatarUrl;
    private String repositoryOwnerLogin;
    private String repositoryFullName;
    private String full_name;
    private String repositoryName;
    private Boolean active;
    private String owner;
    private WebHook webHook;
    private String hmacSecret;
    private String htmlURL;


    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getRepositoryURL() {
        return repositoryURL;
    }

    public void setRepositoryURL(String repositoryURL) {
        this.repositoryURL = repositoryURL;
    }

    public String getRepositoryOwnerAvatarUrl() {
        return repositoryOwnerAvatarUrl;
    }

    public void setRepositoryOwnerAvatarUrl(String repositoryOwnerAvatarUrl) {
        this.repositoryOwnerAvatarUrl = repositoryOwnerAvatarUrl;
    }

    public String getRepositoryOwnerLogin() {
        return repositoryOwnerLogin;
    }

    public void setRepositoryOwnerLogin(String repositoryOwnerLogin) {
        this.repositoryOwnerLogin = repositoryOwnerLogin;
    }

    public String getRepositoryFullName() {
        return repositoryFullName;
    }

    public void setRepositoryFullName(String repositoryFullName) {
        this.repositoryFullName = repositoryFullName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public WebHook getWebHook() {
        return webHook;
    }

    public void setWebHook(WebHook webHook) {
        this.webHook = webHook;
    }

    public String getHmacSecret() {
        return hmacSecret;
    }

    public void setHmacSecret(String hmacSecret) {
        this.hmacSecret = hmacSecret;
    }

    public String getHtmlURL() {
        return htmlURL;
    }

    public void setHtmlURL(String htmlURL) {
        this.htmlURL = htmlURL;
    }
}
