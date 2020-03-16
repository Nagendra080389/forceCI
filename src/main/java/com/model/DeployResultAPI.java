package com.model;

import com.sforce.soap.metadata.DeployResult;

public class DeployResultAPI {

    private String id;
    private String url;
    private DeployResult deployResult;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public DeployResult getDeployResult() {
        return deployResult;
    }

    public void setDeployResult(DeployResult deployResult) {
        this.deployResult = deployResult;
    }
}
