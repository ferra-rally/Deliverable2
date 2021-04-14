package it.deliverable2;

import java.util.List;

public class Issue {
    private String name;
    private String startingAffectedVersion;
    private String endingAffectedVersion;
    private List<RepoFile> affects;

    public Issue(String name, String startingAffectedVersion, String endingAffectedVersion) {
        this.name = name;
        this.startingAffectedVersion = startingAffectedVersion;
        this.endingAffectedVersion = endingAffectedVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartingAffectedVersion() {
        return startingAffectedVersion;
    }

    public void setStartingAffectedVersion(String startingAffectedVersion) {
        this.startingAffectedVersion = startingAffectedVersion;
    }

    public String getEndingAffectedVersion() {
        return endingAffectedVersion;
    }

    public void setEndingAffectedVersion(String endingAffectedVersion) {
        this.endingAffectedVersion = endingAffectedVersion;
    }

    public List<RepoFile> getAffects() {
        return affects;
    }

    public void setAffects(List<RepoFile> affects) {
        this.affects = affects;
    }
}
