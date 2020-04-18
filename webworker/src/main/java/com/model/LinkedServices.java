package com.model;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Objects;

public class LinkedServices implements Serializable {

    @Id
    private String id;
    private String name;
    private String userName;
    private String userEmail;
    private String serverURL;
    private String actions;
    private String accessToken;
    private String connect2DeployUser;
    private boolean connected;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public String getConnect2DeployUser() {
        return connect2DeployUser;
    }

    public void setConnect2DeployUser(String connect2DeployUser) {
        this.connect2DeployUser = connect2DeployUser;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkedServices that = (LinkedServices) o;
        return connected == that.connected &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(userEmail, that.userEmail) &&
                Objects.equals(serverURL, that.serverURL) &&
                Objects.equals(actions, that.actions) &&
                Objects.equals(accessToken, that.accessToken) &&
                Objects.equals(connect2DeployUser, that.connect2DeployUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, userName, userEmail, serverURL, actions, accessToken, connect2DeployUser, connected);
    }
}
