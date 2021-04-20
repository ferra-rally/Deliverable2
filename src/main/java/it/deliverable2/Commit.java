package it.deliverable2;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Commit implements Comparable<Commit>{
    private String name;
    private String message;

    //Identifier of the commit
    private String sha;
    private String author;
    private String commitUrl;
    private List<CommitFile> touchedFiles;

    //Commit Date
    private ZonedDateTime date;

    public Commit() {
        this.touchedFiles = new ArrayList<>();
    }

    //public Commit(String sha, String name,)

    public Commit(String name, String message, String sha, ZonedDateTime date) {
        this.name = name;
        this.message = message;
        this.sha = sha;
        this.date = date;
        this.touchedFiles = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
    }

    public List<CommitFile> getTouchedFiles() {
        return touchedFiles;
    }

    public void setTouchedFiles(List<CommitFile> touchedFiles) {
        this.touchedFiles = touchedFiles;
    }

    public void addRepoFile(CommitFile commitFile) {
        this.touchedFiles.add(commitFile);
    }

    @Override
    public int compareTo(Commit commit) {
        return getDate().compareTo(commit.getDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commit commit = (Commit) o;
        return Objects.equals(sha, commit.sha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sha);
    }
}
