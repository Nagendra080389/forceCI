package com.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Document(collection = "RepositoryWrapper")
public class RepositoryWrapper implements Serializable {

    @Id
    private String id;
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
