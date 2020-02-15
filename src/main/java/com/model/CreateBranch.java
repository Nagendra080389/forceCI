package com.model;

public class CreateBranch {

    private String ref;
    private String sha;

    public CreateBranch(String ref, String sha) {
        this.ref = ref;
        this.sha = sha;
    }
}
