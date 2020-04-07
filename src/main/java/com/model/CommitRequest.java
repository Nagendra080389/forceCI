package com.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class CommitRequest implements Serializable {
    private String newBranchName;
    private String branchFromGit;
    private String destinationBranch;
    private Date fromDate;
    private Date toDate;
    private String userConnect2DeployToken;
    private String linkedServiceName;

    public String getNewBranchName() {
        return newBranchName;
    }

    public void setNewBranchName(String newBranchName) {
        this.newBranchName = newBranchName;
    }

    public String getBranchFromGit() {
        return branchFromGit;
    }

    public void setBranchFromGit(String branchFromGit) {
        this.branchFromGit = branchFromGit;
    }

    public String getDestinationBranch() {
        return destinationBranch;
    }

    public void setDestinationBranch(String destinationBranch) {
        this.destinationBranch = destinationBranch;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String getUserConnect2DeployToken() {
        return userConnect2DeployToken;
    }

    public void setUserConnect2DeployToken(String userConnect2DeployToken) {
        this.userConnect2DeployToken = userConnect2DeployToken;
    }

    public String getLinkedServiceName() {
        return linkedServiceName;
    }

    public void setLinkedServiceName(String linkedServiceName) {
        this.linkedServiceName = linkedServiceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommitRequest that = (CommitRequest) o;
        return Objects.equals(newBranchName, that.newBranchName) &&
                Objects.equals(branchFromGit, that.branchFromGit) &&
                Objects.equals(destinationBranch, that.destinationBranch) &&
                Objects.equals(fromDate, that.fromDate) &&
                Objects.equals(toDate, that.toDate) &&
                Objects.equals(userConnect2DeployToken, that.userConnect2DeployToken) &&
                Objects.equals(linkedServiceName, that.linkedServiceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newBranchName, branchFromGit, destinationBranch, fromDate, toDate, userConnect2DeployToken, linkedServiceName);
    }
}
