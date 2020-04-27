package com.backgroundworker.quartzJob;

public enum Status {

    RUNNING("running"),
    FINISHED("finished"),
    NOTSTARTED("notStarted");

    private String text;

    Status(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}
