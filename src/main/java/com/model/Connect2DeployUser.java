package com.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Connect2DeployUser implements Serializable {
    @Id
    private String id;
    private String emailId;
    private String password;
    private String firstName;
    private String lastName;
    private String token;
    private boolean isEnabled;
    private boolean boolEmailVerified;
    private List<LinkedServices> linkedServices;
    private Map<String, String> mapIpAddressAndToken;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Map<String, String> getMapIpAddressAndToken() {
        return mapIpAddressAndToken;
    }

    public void setMapIpAddressAndToken(Map<String, String> mapIpAddressAndToken) {
        this.mapIpAddressAndToken = mapIpAddressAndToken;
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

    public List<LinkedServices> getLinkedServices() {
        return linkedServices;
    }

    public void setLinkedServices(List<LinkedServices> linkedServices) {
        this.linkedServices = linkedServices;
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
                Objects.equals(linkedServices, that.linkedServices) &&
                Objects.equals(mapIpAddressAndToken, that.mapIpAddressAndToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, emailId, password, firstName, lastName, token, isEnabled, boolEmailVerified, linkedServices, mapIpAddressAndToken);
    }
}
