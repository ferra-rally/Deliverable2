package it.deliverable2;

import org.json.JSONObject;

public class RepoFile {
    private String sha;
    private String filename;
    private int bugs = 0;
    private int addition;
    private int deletion;
    private int changes;

    public RepoFile(String sha, String filename) {
        this.sha = sha;
        this.filename = filename;
    }

    public RepoFile(String filename) {
        this.filename = filename;
    }

    public RepoFile(JSONObject jsonObject) {
        sha = jsonObject.getString("sha");
        filename = jsonObject.getString("filename");
        addition = jsonObject.getInt("additions");
        deletion = jsonObject.getInt("deletions");
        changes = jsonObject.getInt("changes");
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getAddition() {
        return addition;
    }

    public void setAddition(int addition) {
        this.addition = addition;
    }

    public int getDeletion() {
        return deletion;
    }

    public void setDeletion(int deletion) {
        this.deletion = deletion;
    }

    public int getChanges() {
        return changes;
    }

    public void setChanges(int changes) {
        this.changes = changes;
    }

    public void addBug() {
        this.bugs++;
    }

    public boolean isBuggy() {
        return bugs > 0;
    }
}
