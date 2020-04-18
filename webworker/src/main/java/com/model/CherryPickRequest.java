package com.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class CherryPickRequest implements Serializable {

    private List<String> lstCommitIdsSelected;
    private String destinationBranch;
    private String newBranch;
    private String gitCloneURL;
    private String repoToken;
    private String repoId;
    private String repoUserName;
    private String ghEnterpriseServerURL;
    private String linkedServiceName;

    public List<String> getLstCommitIdsSelected() {
        return lstCommitIdsSelected;
    }

    public void setLstCommitIdsSelected(List<String> lstCommitIdsSelected) {
        this.lstCommitIdsSelected = lstCommitIdsSelected;
    }

    public String getDestinationBranch() {
        return destinationBranch;
    }

    public void setDestinationBranch(String destinationBranch) {
        this.destinationBranch = destinationBranch;
    }

    public String getNewBranch() {
        return newBranch;
    }

    public void setNewBranch(String newBranch) {
        this.newBranch = newBranch;
    }

    public String getGitCloneURL() {
        return gitCloneURL;
    }

    public void setGitCloneURL(String gitCloneURL) {
        this.gitCloneURL = gitCloneURL;
    }

    public String getRepoToken() {
        return repoToken;
    }

    public void setRepoToken(String repoToken) {
        this.repoToken = repoToken;
    }

    public String getRepoUserName() {
        return repoUserName;
    }

    public void setRepoUserName(String repoUserName) {
        this.repoUserName = repoUserName;
    }

    public String getGhEnterpriseServerURL() {
        return ghEnterpriseServerURL;
    }

    public void setGhEnterpriseServerURL(String ghEnterpriseServerURL) {
        this.ghEnterpriseServerURL = ghEnterpriseServerURL;
    }

    public String getLinkedServiceName() {
        return linkedServiceName;
    }

    public void setLinkedServiceName(String linkedServiceName) {
        this.linkedServiceName = linkedServiceName;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CherryPickRequest that = (CherryPickRequest) o;
        return Objects.equals(lstCommitIdsSelected, that.lstCommitIdsSelected) &&
                Objects.equals(destinationBranch, that.destinationBranch) &&
                Objects.equals(newBranch, that.newBranch) &&
                Objects.equals(gitCloneURL, that.gitCloneURL) &&
                Objects.equals(repoToken, that.repoToken) &&
                Objects.equals(repoId, that.repoId) &&
                Objects.equals(repoUserName, that.repoUserName) &&
                Objects.equals(ghEnterpriseServerURL, that.ghEnterpriseServerURL) &&
                Objects.equals(linkedServiceName, that.linkedServiceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lstCommitIdsSelected, destinationBranch, newBranch, gitCloneURL, repoToken, repoId, repoUserName, ghEnterpriseServerURL, linkedServiceName);
    }
}
