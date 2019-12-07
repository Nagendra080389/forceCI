package com.model;

import java.io.Serializable;

public class Permissions implements Serializable {
    private String pull;

    private String admin;

    private String push;

    public String getPull() {
        return pull;
    }

    public void setPull(String pull) {
        this.pull = pull;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getPush() {
        return push;
    }

    public void setPush(String push) {
        this.push = push;
    }
}