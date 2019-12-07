package com.model;

public class WebHook {
    private String updated_at;

    private String name;

    private String test_url;

    private String active;

    private String created_at;

    private String id;

    private String type;

    private Last_response last_response;

    private Config config;

    private String ping_url;

    private String[] events;

    private String url;

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTest_url() {
        return test_url;
    }

    public void setTest_url(String test_url) {
        this.test_url = test_url;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Last_response getLast_response() {
        return last_response;
    }

    public void setLast_response(Last_response last_response) {
        this.last_response = last_response;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getPing_url() {
        return ping_url;
    }

    public void setPing_url(String ping_url) {
        this.ping_url = ping_url;
    }

    public String[] getEvents() {
        return events;
    }

    public void setEvents(String[] events) {
        this.events = events;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
