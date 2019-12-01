package com.model;

import java.io.Serializable;

public class Repository implements Serializable {
    private String repositoryName;
    private String repositoryUrl;
    private Boolean active;
    private String owner;
    private String webHookId;
    private String webHookUrl;

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

    public String getWebHookId() {
        return webHookId;
    }

    public void setWebHookId(String webHookId) {
        this.webHookId = webHookId;
    }

    public String getWebHookUrl() {
        return webHookUrl;
    }

    public void setWebHookUrl(String webHookUrl) {
        this.webHookUrl = webHookUrl;
    }
}
