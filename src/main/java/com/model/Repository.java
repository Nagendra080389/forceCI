package com.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Repository implements Serializable {
    private String repositoryId;
    private String repositoryURL;
    private String repositoryOwnerAvatarUrl;
    private String repositoryOwnerLogin;
    private String repositoryFullName;
    private String full_name;
    private String repositoryName;
    private Boolean active;
    private String owner;
    private WebHook webHook;
    private String hmacSecret;
    private String htmlURL;


    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getRepositoryURL() {
        return repositoryURL;
    }

    public void setRepositoryURL(String repositoryURL) {
        this.repositoryURL = repositoryURL;
    }

    public String getRepositoryOwnerAvatarUrl() {
        return repositoryOwnerAvatarUrl;
    }

    public void setRepositoryOwnerAvatarUrl(String repositoryOwnerAvatarUrl) {
        this.repositoryOwnerAvatarUrl = repositoryOwnerAvatarUrl;
    }

    public String getRepositoryOwnerLogin() {
        return repositoryOwnerLogin;
    }

    public void setRepositoryOwnerLogin(String repositoryOwnerLogin) {
        this.repositoryOwnerLogin = repositoryOwnerLogin;
    }

    public String getRepositoryFullName() {
        return repositoryFullName;
    }

    public void setRepositoryFullName(String repositoryFullName) {
        this.repositoryFullName = repositoryFullName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public WebHook getWebHook() {
        return webHook;
    }

    public void setWebHook(WebHook webHook) {
        this.webHook = webHook;
    }

    public String getHmacSecret() {
        return hmacSecret;
    }

    public void setHmacSecret(String hmacSecret) {
        this.hmacSecret = hmacSecret;
    }

    public String getHtmlURL() {
        return htmlURL;
    }

    public void setHtmlURL(String htmlURL) {
        this.htmlURL = htmlURL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Repository)) return false;
        Repository that = (Repository) o;
        return repositoryId.equals(that.repositoryId) &&
                repositoryURL.equals(that.repositoryURL) &&
                repositoryOwnerAvatarUrl.equals(that.repositoryOwnerAvatarUrl) &&
                repositoryOwnerLogin.equals(that.repositoryOwnerLogin) &&
                repositoryFullName.equals(that.repositoryFullName) &&
                full_name.equals(that.full_name) &&
                repositoryName.equals(that.repositoryName) &&
                active.equals(that.active) &&
                owner.equals(that.owner) &&
                webHook.equals(that.webHook) &&
                hmacSecret.equals(that.hmacSecret) &&
                htmlURL.equals(that.htmlURL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryId, repositoryURL, repositoryOwnerAvatarUrl, repositoryOwnerLogin, repositoryFullName, full_name, repositoryName, active, owner, webHook, hmacSecret, htmlURL);
    }
}
