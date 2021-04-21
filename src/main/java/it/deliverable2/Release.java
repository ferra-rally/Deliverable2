package it.deliverable2;

import java.time.ZonedDateTime;
import java.util.*;

public class Release implements Comparable<Release> {
    private String commitUrl;
    private String name;
    private String fullName;
    private int number;
    //Commit of the tag
    private Commit commit;
    private List<ReleaseFile> fileList;
    private Map<String, ReleaseFile> fileMap;

    private List<Commit> commits;
    private ZonedDateTime date;

    public Release(String name, String commitUrl) {
        this.setName(name);
        this.commitUrl = commitUrl;
    }

    //Used by jira
    public Release(String name, ZonedDateTime dateTime) {
        this.name = name;
        this.date = dateTime;

        this.fullName = "release-" + this.name;
    }

    public void setBugs(List<CommitFile> issueFiles) {
        List<String> issueFileNames = new ArrayList<>();
        for (CommitFile file : issueFiles) {
            issueFileNames.add(file.getFilename());
        }

        for (ReleaseFile file : fileList) {
            if (issueFileNames.contains(file.getFilename())) {
                file.addBug();
            }
        }
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
        if(number >= 0) {
            return number;
        }
        return -1;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<ReleaseFile> getFileList() {
        return fileList;
    }

    public void setFileMap(Map<String, ReleaseFile> fileMap) {
        this.fileMap = fileMap;

        this.fileList = new ArrayList<>(fileMap.values());
    }

    public Map<String, ReleaseFile> getFileMap() {
        return fileMap;
    }

    @Override
    public int compareTo(Release rel) {
        return getDate().compareTo(rel.getDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Release release = (Release) o;
        return number == release.number && Objects.equals(name, release.name) && Objects.equals(date, release.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commitUrl, name, fullName, number, commit, fileList, commits, date);
    }
}
