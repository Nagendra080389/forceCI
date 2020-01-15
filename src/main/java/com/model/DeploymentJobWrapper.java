package com.model;

import java.io.Serializable;

public class DeploymentJobWrapper implements Serializable {
    private String id;
    private String jobNo;
    private String prNumber;
    private boolean boolSfdcValidationRunning;
    private boolean boolSfdcValidationPass;
    private boolean boolSfdcValidationFail;
    private String sfdcValidationRunning;
    private String sfdcValidationPass;
    private String sfdcValidationFail;
    private boolean boolCodeReviewValidationRunning;
    private boolean boolCodeReviewValidationPass;
    private boolean boolCodeReviewValidationFail;
    private String codeReviewValidationRunning;
    private String codeReviewValidationPass;
    private String codeReviewValidationFail;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobNo() {
        return jobNo;
    }

    public void setJobNo(String jobNo) {
        this.jobNo = jobNo;
    }

    public String getPrNumber() {
        return prNumber;
    }

    public void setPrNumber(String prNumber) {
        this.prNumber = prNumber;
    }

    public boolean isBoolSfdcValidationRunning() {
        return boolSfdcValidationRunning;
    }

    public void setBoolSfdcValidationRunning(boolean boolSfdcValidationRunning) {
        this.boolSfdcValidationRunning = boolSfdcValidationRunning;
    }

    public boolean isBoolSfdcValidationPass() {
        return boolSfdcValidationPass;
    }

    public void setBoolSfdcValidationPass(boolean boolSfdcValidationPass) {
        this.boolSfdcValidationPass = boolSfdcValidationPass;
    }

    public boolean isBoolSfdcValidationFail() {
        return boolSfdcValidationFail;
    }

    public void setBoolSfdcValidationFail(boolean boolSfdcValidationFail) {
        this.boolSfdcValidationFail = boolSfdcValidationFail;
    }

    public String getSfdcValidationRunning() {
        return sfdcValidationRunning;
    }

    public void setSfdcValidationRunning(String sfdcValidationRunning) {
        this.sfdcValidationRunning = sfdcValidationRunning;
    }

    public String getSfdcValidationPass() {
        return sfdcValidationPass;
    }

    public void setSfdcValidationPass(String sfdcValidationPass) {
        this.sfdcValidationPass = sfdcValidationPass;
    }

    public String getSfdcValidationFail() {
        return sfdcValidationFail;
    }

    public void setSfdcValidationFail(String sfdcValidationFail) {
        this.sfdcValidationFail = sfdcValidationFail;
    }

    public boolean isBoolCodeReviewValidationRunning() {
        return boolCodeReviewValidationRunning;
    }

    public void setBoolCodeReviewValidationRunning(boolean boolCodeReviewValidationRunning) {
        this.boolCodeReviewValidationRunning = boolCodeReviewValidationRunning;
    }

    public boolean isBoolCodeReviewValidationPass() {
        return boolCodeReviewValidationPass;
    }

    public void setBoolCodeReviewValidationPass(boolean boolCodeReviewValidationPass) {
        this.boolCodeReviewValidationPass = boolCodeReviewValidationPass;
    }

    public boolean isBoolCodeReviewValidationFail() {
        return boolCodeReviewValidationFail;
    }

    public void setBoolCodeReviewValidationFail(boolean boolCodeReviewValidationFail) {
        this.boolCodeReviewValidationFail = boolCodeReviewValidationFail;
    }

    public String getCodeReviewValidationRunning() {
        return codeReviewValidationRunning;
    }

    public void setCodeReviewValidationRunning(String codeReviewValidationRunning) {
        this.codeReviewValidationRunning = codeReviewValidationRunning;
    }

    public String getCodeReviewValidationPass() {
        return codeReviewValidationPass;
    }

    public void setCodeReviewValidationPass(String codeReviewValidationPass) {
        this.codeReviewValidationPass = codeReviewValidationPass;
    }

    public String getCodeReviewValidationFail() {
        return codeReviewValidationFail;
    }

    public void setCodeReviewValidationFail(String codeReviewValidationFail) {
        this.codeReviewValidationFail = codeReviewValidationFail;
    }
}
