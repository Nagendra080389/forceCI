package com.model;

import java.io.Serializable;
import java.util.Objects;

public class Owner implements Serializable {
    private String gists_url;

    private String repos_url;

    private String following_url;

    private String starred_url;

    private String login;

    private String followers_url;

    private String type;

    private String url;

    private String subscriptions_url;

    private String received_events_url;

    private String avatar_url;

    private String events_url;

    private String html_url;

    private String site_admin;

    private String id;

    private String gravatar_id;

    private String node_id;

    private String organizations_url;

    public String getGists_url() {
        return gists_url;
    }

    public void setGists_url(String gists_url) {
        this.gists_url = gists_url;
    }

    public String getRepos_url() {
        return repos_url;
    }

    public void setRepos_url(String repos_url) {
        this.repos_url = repos_url;
    }

    public String getFollowing_url() {
        return following_url;
    }

    public void setFollowing_url(String following_url) {
        this.following_url = following_url;
    }

    public String getStarred_url() {
        return starred_url;
    }

    public void setStarred_url(String starred_url) {
        this.starred_url = starred_url;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFollowers_url() {
        return followers_url;
    }

    public void setFollowers_url(String followers_url) {
        this.followers_url = followers_url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSubscriptions_url() {
        return subscriptions_url;
    }

    public void setSubscriptions_url(String subscriptions_url) {
        this.subscriptions_url = subscriptions_url;
    }

    public String getReceived_events_url() {
        return received_events_url;
    }

    public void setReceived_events_url(String received_events_url) {
        this.received_events_url = received_events_url;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getEvents_url() {
        return events_url;
    }

    public void setEvents_url(String events_url) {
        this.events_url = events_url;
    }

    public String getHtml_url() {
        return html_url;
    }

    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }

    public String getSite_admin() {
        return site_admin;
    }

    public void setSite_admin(String site_admin) {
        this.site_admin = site_admin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGravatar_id() {
        return gravatar_id;
    }

    public void setGravatar_id(String gravatar_id) {
        this.gravatar_id = gravatar_id;
    }

    public String getNode_id() {
        return node_id;
    }

    public void setNode_id(String node_id) {
        this.node_id = node_id;
    }

    public String getOrganizations_url() {
        return organizations_url;
    }

    public void setOrganizations_url(String organizations_url) {
        this.organizations_url = organizations_url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Owner owner = (Owner) o;
        return Objects.equals(gists_url, owner.gists_url) &&
                Objects.equals(repos_url, owner.repos_url) &&
                Objects.equals(following_url, owner.following_url) &&
                Objects.equals(starred_url, owner.starred_url) &&
                Objects.equals(login, owner.login) &&
                Objects.equals(followers_url, owner.followers_url) &&
                Objects.equals(type, owner.type) &&
                Objects.equals(url, owner.url) &&
                Objects.equals(subscriptions_url, owner.subscriptions_url) &&
                Objects.equals(received_events_url, owner.received_events_url) &&
                Objects.equals(avatar_url, owner.avatar_url) &&
                Objects.equals(events_url, owner.events_url) &&
                Objects.equals(html_url, owner.html_url) &&
                Objects.equals(site_admin, owner.site_admin) &&
                Objects.equals(id, owner.id) &&
                Objects.equals(gravatar_id, owner.gravatar_id) &&
                Objects.equals(node_id, owner.node_id) &&
                Objects.equals(organizations_url, owner.organizations_url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gists_url, repos_url, following_url, starred_url, login, followers_url, type, url, subscriptions_url, received_events_url, avatar_url, events_url, html_url, site_admin, id, gravatar_id, node_id, organizations_url);
    }
}
