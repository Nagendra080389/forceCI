package com.model;

import java.io.Serializable;

public class DeployResult implements Serializable {
    private String numberComponentsTotal;

    private String numberTestErrors;

    private String createdByName;

    private String numberComponentErrors;

    private String numberTestsTotal;

    private String numberTestsCompleted;

    private String runTestsEnabled;

    private String numberComponentsDeployed;
    private String id;

    private String ignoreWarnings;

    private String lastModifiedDate;
    private String checkOnly;

    private String done;

    private String completedDate;

    private String createdDate;

    private String rollbackOnError;

    private String createdBy;

    private String success;

    private String startDate;

    private String status;

    public String getNumberComponentsTotal() {
        return numberComponentsTotal;
    }

    public void setNumberComponentsTotal(String numberComponentsTotal) {
        this.numberComponentsTotal = numberComponentsTotal;
    }

    public String getNumberTestErrors() {
        return numberTestErrors;
    }

    public void setNumberTestErrors(String numberTestErrors) {
        this.numberTestErrors = numberTestErrors;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getNumberComponentErrors() {
        return numberComponentErrors;
    }

    public void setNumberComponentErrors(String numberComponentErrors) {
        this.numberComponentErrors = numberComponentErrors;
    }

    public String getNumberTestsTotal() {
        return numberTestsTotal;
    }

    public void setNumberTestsTotal(String numberTestsTotal) {
        this.numberTestsTotal = numberTestsTotal;
    }

    public String getNumberTestsCompleted() {
        return numberTestsCompleted;
    }

    public void setNumberTestsCompleted(String numberTestsCompleted) {
        this.numberTestsCompleted = numberTestsCompleted;
    }

    public String getRunTestsEnabled() {
        return runTestsEnabled;
    }

    public void setRunTestsEnabled(String runTestsEnabled) {
        this.runTestsEnabled = runTestsEnabled;
    }

    public String getNumberComponentsDeployed() {
        return numberComponentsDeployed;
    }

    public void setNumberComponentsDeployed(String numberComponentsDeployed) {
        this.numberComponentsDeployed = numberComponentsDeployed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIgnoreWarnings() {
        return ignoreWarnings;
    }

    public void setIgnoreWarnings(String ignoreWarnings) {
        this.ignoreWarnings = ignoreWarnings;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCheckOnly() {
        return checkOnly;
    }

    public void setCheckOnly(String checkOnly) {
        this.checkOnly = checkOnly;
    }

    public String getDone() {
        return done;
    }

    public void setDone(String done) {
        this.done = done;
    }

    public String getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getRollbackOnError() {
        return rollbackOnError;
    }

    public void setRollbackOnError(String rollbackOnError) {
        this.rollbackOnError = rollbackOnError;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
