package it.deliverable2;

import org.json.JSONObject;

//Class that stores the commit file info
public class CommitFile {
    private String sha;
    private String filename;
    private int addition = 0;
    private int deletion = 0;

    public CommitFile(String filename, int addition, int deletion) {
        this.filename = filename;
        this.addition = addition;
        this.deletion = deletion;
    }

    public CommitFile(JSONObject jsonObject) {
        sha = jsonObject.getString("sha");
        filename = jsonObject.getString("filename");
        addition = jsonObject.getInt("additions");
        deletion = jsonObject.getInt("deletions");
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

    public int getAddition() {
        return addition;
    }

    public int getDeletion() {
        return deletion;
    }

    public int getChanges() {
        return addition + deletion;
    }
}
