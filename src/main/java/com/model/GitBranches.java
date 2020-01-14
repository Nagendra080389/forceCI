package com.model;

import java.io.Serializable;

public class GitBranches implements Serializable {

    private String name;

    private Commit commit;

    private Protection protection;

    private String protection_url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Commit getCommit() {
        return commit;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }

    public Protection getProtection() {
        return protection;
    }

    public void setProtection(Protection protection) {
        this.protection = protection;
    }

    public String getProtection_url() {
        return protection_url;
    }

    public void setProtection_url(String protection_url) {
        this.protection_url = protection_url;
    }

}
