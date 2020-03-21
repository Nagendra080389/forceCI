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
    private Connect2DeployUser user;

    public Connect2DeployToken(Connect2DeployUser user) {
        this.user = user;
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

    public Connect2DeployUser getUser() {
        return user;
    }

    public void setUser(Connect2DeployUser user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connect2DeployToken that = (Connect2DeployToken) o;
        return id == that.id &&
                confirmationToken.equals(that.confirmationToken) &&
                createdDate.equals(that.createdDate) &&
                user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, confirmationToken, createdDate, user);
    }
}
