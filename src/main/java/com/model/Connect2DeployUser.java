package com.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;

import java.io.Serializable;
import java.util.Objects;

public class Connect2DeployUser implements Serializable {
    @Id
    private long userid;
    private String emailId;
    private String password;
    private String firstName;
    private String lastName;
    private boolean isEnabled;

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connect2DeployUser that = (Connect2DeployUser) o;
        return userid == that.userid &&
                isEnabled == that.isEnabled &&
                emailId.equals(that.emailId) &&
                password.equals(that.password) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userid, emailId, password, firstName, lastName, isEnabled);
    }
}
