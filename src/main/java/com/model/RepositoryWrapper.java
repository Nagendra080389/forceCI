package com.model;

import java.io.Serializable;
import java.util.List;

public class RepositoryWrapper implements Serializable {

    private List<Repository> lstRepositories;
    private String ownerId;

    public List<Repository> getLstRepositories() {
        return lstRepositories;
    }

    public void setLstRepositories(List<Repository> lstRepositories) {
        this.lstRepositories = lstRepositories;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
