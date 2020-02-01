package com.model;

import java.io.Serializable;

public class GithubStatusObject implements Serializable {
    private String state;
    private String description;
    private String context;

    public GithubStatusObject(String state, String description, String context) {
        this.state = state;
        this.description = description;
        this.context = context;
    }
}
