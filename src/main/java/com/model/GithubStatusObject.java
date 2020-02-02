package com.model;

import java.io.Serializable;

public class GithubStatusObject implements Serializable {
    private String state;
    private String description;
    private String context;
    private String target_url;

    public GithubStatusObject(String state, String description, String context, String target_url) {
        this.state = state;
        this.description = description;
        this.context = context;
        this.target_url = target_url;
    }
}
