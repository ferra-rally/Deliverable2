package it.deliverable2;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RepoFile {
    private String sha;
    private String filename;
    //Store number of bugs
    private int bugs = 0;
    private int addition = 0;
    private int deletion = 0;
    //TODO is this modified  or is (addition - deletion) ??
    private int loc;
    private int numOfRevision = 0;
    private List<String> authorList;

    public RepoFile(String sha, String filename) {
        this.sha = sha;
        this.filename = filename;
    }

    public RepoFile(String filename) {
        this.filename = filename;
    }

    public RepoFile(String filename, int addition, int deletion) {
        this.filename = filename;
        this.addition = addition;
        this.deletion = deletion;
    }

    public RepoFile(JSONObject jsonObject) {
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
        return addition + deletion;
    }

    public void addBug() {
        this.bugs++;
    }

    public String isBuggy() {
        if(bugs > 0) {
            return "Yes";
        } else {
            return "No";
        }
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public int getNumOfRevision() {
        return numOfRevision;
    }

    public void setNumOfRevision(int numOfRevision) {
        this.numOfRevision = numOfRevision;
    }

    public int getNumOfAuthors() {
        if(authorList == null) {
            return 0;
        }

        return authorList.size();
    }

    public void addAuthor(String author) {
        if(authorList == null) {
            this.authorList = new ArrayList<>();
        }

        if(!authorList.contains(author)) {
            authorList.add(author);
        }
    }

    public void addAdded(int addition) {
        this.addition += addition;
    }

    public void addDeletion(int deletion) {
        this.deletion += deletion;
    }

    public void addRevision() {
        this.numOfRevision++;
    }
}
