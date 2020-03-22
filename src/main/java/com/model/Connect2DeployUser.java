package com.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;

import java.io.Serializable;
import java.util.Objects;

public class Connect2DeployUser implements Serializable {
    @Id
    private String id;
    private String emailId;
    private String password;
    private String firstName;
    private String lastName;
    private boolean isEnabled;
    private boolean boolEmailVerified;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isBoolEmailVerified() {
        return boolEmailVerified;
    }

    public void setBoolEmailVerified(boolean boolEmailVerified) {
        this.boolEmailVerified = boolEmailVerified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connect2DeployUser that = (Connect2DeployUser) o;
        return isEnabled == that.isEnabled &&
                id.equals(that.id) &&
                emailId.equals(that.emailId) &&
                password.equals(that.password) &&
                firstName.equals(that.firstName) &&
                Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, emailId, password, firstName, lastName, isEnabled);
    }
}
