package it.deliverable2;

import java.time.ZonedDateTime;
import java.util.List;

public class Release {
    private String commitUrl;
    private String name;
    private int number;
    //Commit of the tag
    private Commit commit;

    private List<Commit> commits;
    private ZonedDateTime date;

    public Release(String name, String commitUrl) {
        this.setName(name);
        this.commitUrl = commitUrl;
    }

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.split("-")[1];
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public Commit getCommit() {
        return commit;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }
}
