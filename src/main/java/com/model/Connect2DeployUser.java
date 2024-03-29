package com.model;

import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;
import java.util.*;

public class Connect2DeployUser implements Serializable {
    @Id
    private String id;
    @Indexed
    private String emailId;
    private String password;
    private String firstName;
    private String lastName;
    private String token;
    @BsonIgnore
    private String googleReCaptchaV3;
    private Date tokenExpirationTime;
    private boolean isEnabled;
    private boolean boolEmailVerified;
    private Set<String> linkedServices;

    public String getGoogleReCaptchaV3() {
        return googleReCaptchaV3;
    }

    public void setGoogleReCaptchaV3(String googleReCaptchaV3) {
        this.googleReCaptchaV3 = googleReCaptchaV3;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

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

    public Set<String> getLinkedServices() {
        return linkedServices;
    }

    public void setLinkedServices(Set<String> linkedServices) {
        this.linkedServices = linkedServices;
    }

    public Date getTokenExpirationTime() {
        return tokenExpirationTime;
    }

    public void setTokenExpirationTime(Date tokenExpirationTime) {
        this.tokenExpirationTime = tokenExpirationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connect2DeployUser that = (Connect2DeployUser) o;
        return isEnabled == that.isEnabled &&
                boolEmailVerified == that.boolEmailVerified &&
                Objects.equals(id, that.id) &&
                Objects.equals(emailId, that.emailId) &&
                Objects.equals(password, that.password) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(token, that.token) &&
                Objects.equals(tokenExpirationTime, that.tokenExpirationTime) &&
                Objects.equals(linkedServices, that.linkedServices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, emailId, password, firstName, lastName, token, tokenExpirationTime, isEnabled, boolEmailVerified, linkedServices);
    }
}
