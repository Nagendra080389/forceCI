package com.model;


import java.io.Serializable;

public class DeployResultAPI implements Serializable {
    private DeployResult deployResult;
    private String id;

    public DeployResult getDeployResult() {
        return deployResult;
    }

    public void setDeployResult(DeployResult deployResult) {
        this.deployResult = deployResult;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
