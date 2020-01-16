package com.utils;

public enum ValidationStatus {
    VALIDATION_RUNNING("validationRunning"), VALIDATION_PASS("validationPass"), VALIDATION_FAIL("validationFail");

    private String text;

    ValidationStatus(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}