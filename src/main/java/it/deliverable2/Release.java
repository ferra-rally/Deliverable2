package it.deliverable2;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Release implements Comparable<Release>{
    private String commitUrl;
    private String name;
    private String fullName;
    private int number;
    //Commit of the tag
    private Commit commit;
    private List<RepoFile> fileList;

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

    public void setBugs(List<RepoFile> issueFiles) {
        List<String> issueFileNames = new ArrayList<>();
        for(RepoFile file : issueFiles) {
            issueFileNames.add(file.getFilename());
        }

        for(RepoFile file : fileList) {
            if(issueFileNames.contains(file.getFilename())) file.addBug();
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
        //this.name = name.split("-")[1];
        this.name = name;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<RepoFile> getFileList() {
        return fileList;
    }

    public void setFileList(List<RepoFile> fileList) {
        this.fileList = fileList;
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
        return number == release.number && Objects.equals(commitUrl, release.commitUrl) && Objects.equals(name, release.name) && Objects.equals(fullName, release.fullName) && Objects.equals(commit, release.commit) && Objects.equals(fileList, release.fileList) && Objects.equals(commits, release.commits) && Objects.equals(date, release.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commitUrl, name, fullName, number, commit, fileList, commits, date);
    }
}
