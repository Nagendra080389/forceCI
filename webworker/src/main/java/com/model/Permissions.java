package com.model;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permissions that = (Permissions) o;
        return Objects.equals(pull, that.pull) &&
                Objects.equals(admin, that.admin) &&
                Objects.equals(push, that.push);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pull, admin, push);
    }
}