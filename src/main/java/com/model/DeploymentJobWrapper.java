package com.model;

import java.io.Serializable;

public class DeploymentJobWrapper implements Serializable, Comparable<DeploymentJobWrapper> {
    private String id;
    private String jobNo;
    private String prNumber;
    private String prHtml;
    private String sourceBranch;
    private String packageXML;
    private boolean boolSfdcValidationRunning;
    private boolean boolSfdcValidationPass;
    private boolean boolSfdcValidationFail;
    private String sfdcValidationRunning;
    private String sfdcValidationPass;
    private String sfdcValidationFail;
    private boolean boolCodeReviewValidationRunning;
    private boolean boolCodeReviewValidationPass;
    private boolean boolCodeReviewValidationFail;
    private boolean boolCodeReviewNotStarted;
    private boolean boolSFDCDeploymentRunning;
    private boolean boolSFDCDeploymentPass;
    private boolean boolSFDCDeploymentFail;
    private boolean boolSFDCDeploymentNotStarted;
    private String sfdcDeploymentRunning;
    private String sfdcDeploymentPass;
    private String sfdcDeploymentFail;
    private String sfdcDeploymentNotStarted;
    private String codeReviewValidationRunning;
    private String codeReviewValidationPass;
    private String codeReviewValidationFail;
    private String codeReviewValidationNotStarted;

    public String getSourceBranch() {
        return sourceBranch;
    }

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public String getPackageXML() {
        return packageXML;
    }

    public void setPackageXML(String packageXML) {
        this.packageXML = packageXML;
    }

    public boolean isBoolSFDCDeploymentRunning() {
        return boolSFDCDeploymentRunning;
    }

    public void setBoolSFDCDeploymentRunning(boolean boolSFDCDeploymentRunning) {
        this.boolSFDCDeploymentRunning = boolSFDCDeploymentRunning;
    }

    public boolean isBoolSFDCDeploymentPass() {
        return boolSFDCDeploymentPass;
    }

    public void setBoolSFDCDeploymentPass(boolean boolSFDCDeploymentPass) {
        this.boolSFDCDeploymentPass = boolSFDCDeploymentPass;
    }

    public boolean isBoolSFDCDeploymentFail() {
        return boolSFDCDeploymentFail;
    }

    public void setBoolSFDCDeploymentFail(boolean boolSFDCDeploymentFail) {
        this.boolSFDCDeploymentFail = boolSFDCDeploymentFail;
    }

    public boolean isBoolSFDCDeploymentNotStarted() {
        return boolSFDCDeploymentNotStarted;
    }

    public void setBoolSFDCDeploymentNotStarted(boolean boolSFDCDeploymentNotStarted) {
        this.boolSFDCDeploymentNotStarted = boolSFDCDeploymentNotStarted;
    }

    public String getSfdcDeploymentRunning() {
        return sfdcDeploymentRunning;
    }

    public void setSfdcDeploymentRunning(String sfdcDeploymentRunning) {
        this.sfdcDeploymentRunning = sfdcDeploymentRunning;
    }

    public String getSfdcDeploymentPass() {
        return sfdcDeploymentPass;
    }

    public void setSfdcDeploymentPass(String sfdcDeploymentPass) {
        this.sfdcDeploymentPass = sfdcDeploymentPass;
    }

    public String getSfdcDeploymentFail() {
        return sfdcDeploymentFail;
    }

    public void setSfdcDeploymentFail(String sfdcDeploymentFail) {
        this.sfdcDeploymentFail = sfdcDeploymentFail;
    }

    public String getSfdcDeploymentNotStarted() {
        return sfdcDeploymentNotStarted;
    }

    public void setSfdcDeploymentNotStarted(String sfdcDeploymentNotStarted) {
        this.sfdcDeploymentNotStarted = sfdcDeploymentNotStarted;
    }

    public String getCodeReviewValidationNotStarted() {
        return codeReviewValidationNotStarted;
    }

    public void setCodeReviewValidationNotStarted(String codeReviewValidationNotStarted) {
        this.codeReviewValidationNotStarted = codeReviewValidationNotStarted;
    }

    public String getPrHtml() {
        return prHtml;
    }

    public void setPrHtml(String prHtml) {
        this.prHtml = prHtml;
    }

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

    public boolean isBoolCodeReviewNotStarted() {
        return boolCodeReviewNotStarted;
    }

    public void setBoolCodeReviewNotStarted(boolean boolCodeReviewNotStarted) {
        this.boolCodeReviewNotStarted = boolCodeReviewNotStarted;
    }

    @Override
    public int compareTo(DeploymentJobWrapper deploymentJobWrapper) {
        if (Integer.parseInt(deploymentJobWrapper.getJobNo()) == Integer.parseInt(this.jobNo)) {
            return 0;
        } else if (Integer.parseInt(deploymentJobWrapper.getJobNo()) > Integer.parseInt(this.jobNo)) {
            return 1;
        } else {
            return -1;
        }
    }
}
