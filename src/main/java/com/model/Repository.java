package com.model;

import java.io.Serializable;
import java.util.List;

public class Repository implements Serializable {
    private String repositoryId;
    private String full_name;
    private String repositoryName;
    private String repositoryUrl;
    private Boolean active;
    private String owner;
    private WebHook webHook;
    private String hmacSecret;


    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
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
}
