package it.deliverable2;

public class RepoFile {
    private String sha;
    private String filename;
    private int addition;
    private int deletion;
    private int changes;

    public RepoFile(String sha, String filename, int addition, int deletion, int changes) {
        this.sha = sha;
        this.filename = filename;
        this.addition = addition;
        this.deletion = deletion;
        this.changes = changes;
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
}
