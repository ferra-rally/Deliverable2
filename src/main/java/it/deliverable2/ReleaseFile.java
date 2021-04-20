package it.deliverable2;

import java.util.ArrayList;
import java.util.List;

//Class that stores the release file info
public class ReleaseFile {
    private int bugs = 0;
    private int loc;
    //TODO is this modified  or is (addition - deletion) ??
    private int addition;
    private int deletion;
    private String filename;
    private int numOfRevision = 0;
    private List<String> authorList;

    public ReleaseFile(String filename) {
        this.filename = filename;
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

    public void addBug() {
        this.bugs++;
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
