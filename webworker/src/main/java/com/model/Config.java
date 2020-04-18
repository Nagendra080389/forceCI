package com.model;

public class Config {

    private String content_type;

    private String insecure_ssl;

    private String url;
    private String secret;

    public String getContent_type() {
        return content_type;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public String getInsecure_ssl() {
        return insecure_ssl;
    }

    public void setInsecure_ssl(String insecure_ssl) {
        this.insecure_ssl = insecure_ssl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
