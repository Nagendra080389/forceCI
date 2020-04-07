package com.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class CommitResponse implements Serializable {
    private String commitId;
    private String authorName;
    private String authorUrl;
    private String commitURL;
    private String committerName;
    private String committerURL;
    private String commitMessage;
    private Date commitDate;

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getCommitURL() {
        return commitURL;
    }

    public void setCommitURL(String commitURL) {
        this.commitURL = commitURL;
    }

    public String getCommitterName() {
        return committerName;
    }

    public void setCommitterName(String committerName) {
        this.committerName = committerName;
    }

    public String getCommitterURL() {
        return committerURL;
    }

    public void setCommitterURL(String committerURL) {
        this.committerURL = committerURL;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommitResponse that = (CommitResponse) o;
        return Objects.equals(commitId, that.commitId) &&
                Objects.equals(authorName, that.authorName) &&
                Objects.equals(authorUrl, that.authorUrl) &&
                Objects.equals(commitURL, that.commitURL) &&
                Objects.equals(committerName, that.committerName) &&
                Objects.equals(committerURL, that.committerURL) &&
                Objects.equals(commitDate, that.commitDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commitId, authorName, authorUrl, commitURL, committerName, committerURL, commitDate);
    }
}
