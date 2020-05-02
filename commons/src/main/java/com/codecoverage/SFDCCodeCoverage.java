package com.codecoverage;

import java.io.Serializable;
import java.util.Objects;

public class SFDCCodeCoverage implements Serializable {

    private String scheduledJobId;
    private String methodName;
    private String runTime;
    private String errorMessage;
    private String stacktrace;
    private Boolean boolPassed;

    public String getScheduledJobId() {
        return scheduledJobId;
    }

    public void setScheduledJobId(String scheduledJobId) {
        this.scheduledJobId = scheduledJobId;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Boolean getBoolPassed() {
        return boolPassed;
    }

    public void setBoolPassed(Boolean boolPassed) {
        this.boolPassed = boolPassed;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SFDCCodeCoverage that = (SFDCCodeCoverage) o;
        return Objects.equals(scheduledJobId, that.scheduledJobId) &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(runTime, that.runTime) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(stacktrace, that.stacktrace) &&
                Objects.equals(boolPassed, that.boolPassed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduledJobId, methodName, runTime, errorMessage, stacktrace, boolPassed);
    }
}
