package com.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class Connect2DeployToken implements Serializable {

    @Id
    private String id;
    private String confirmationToken;
    @CreatedDate
    private Date createdDate;
    private String userId;

    public Connect2DeployToken(String userId) {
        this.userId = userId;
        createdDate = new Date();
        confirmationToken = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConfirmationToken() {
        return confirmationToken;
    }

    public void setConfirmationToken(String confirmationToken) {
        this.confirmationToken = confirmationToken;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connect2DeployToken that = (Connect2DeployToken) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(confirmationToken, that.confirmationToken) &&
                Objects.equals(createdDate, that.createdDate) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, confirmationToken, createdDate, userId);
    }
}
