package it.deliverable2;

import java.util.List;

public class Release {
    private String commitUrl;
    private String name;
    private int number;
    private List<Commit> commits;

    public Release(String name, String commitUrl) {
        this.name = name;
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
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
