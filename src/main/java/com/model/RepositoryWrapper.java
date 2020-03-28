package com.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Objects;

@Document(collection = "RepositoryWrapper")
public class RepositoryWrapper implements Serializable {

    @Id
    private String id;
    private Repository repository;
    @Indexed
    private String ownerId;
    private String linkedService;


    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getLinkedService() {
        return linkedService;
    }

    public void setLinkedService(String linkedService) {
        this.linkedService = linkedService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepositoryWrapper that = (RepositoryWrapper) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(repository, that.repository) &&
                Objects.equals(ownerId, that.ownerId) &&
                Objects.equals(linkedService, that.linkedService);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, repository, ownerId, linkedService);
    }
}
