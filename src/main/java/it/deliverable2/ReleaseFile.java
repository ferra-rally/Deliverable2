package it.deliverable2;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

//Class that stores the release file info
public class ReleaseFile {
    private int bugs = 0;
    private int fixes = 0;
    private int loc;
    private ZonedDateTime insertDate;

    private int addition;
    private int deletion;
    private String filename;

    private List<Integer> addedList;
    private List<Integer> churnList;
    private List<Integer> chgSetList;

    private int numOfRevision = 0;

    private List<String> authorList;

    public ReleaseFile(String filename) {
        this.filename = filename;

        this.addedList = new ArrayList<>();
        this.churnList = new ArrayList<>();
        this.chgSetList = new ArrayList<>();
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

    public void addAddedAndDelition(int addition, int deletion) {
        this.addition += addition;
        this.deletion += deletion;

        this.churnList.add(addition - deletion);

        this.addedList.add(addition);
    }

    public void addRevision() {
        this.numOfRevision++;
    }

    public Integer getLocAdded() {
        return this.addition;
    }

    private Integer max(List<Integer> list) {
        if(list.isEmpty()) return 0;

        Integer max = Integer.MIN_VALUE;
        for(Integer x : list) {
            if(x > max) {
                max = x;
            }
        }

        return max;
    }

    public Integer getMaxLocAdded() {
        return max(this.addedList);
    }

    public double getAvgLocAdded() {
        double avg = ((this.addition * 1.0) / this.addedList.size());

        if(Double.isNaN(avg)) return 0;

        return avg;
    }

    public Integer getChurn() {
        return this.addition - this.deletion;
    }

    public Integer getMaxChurn() {
        return max(this.churnList);
    }

    public double getAvgChurn() {
        double avg = (((this.addition - this.deletion) * 1.0) / this.churnList.size());

        if(Double.isNaN(avg)) return 0;

        return avg;
    }

    public void addChgSetSize(Integer chgSet) {
        this.chgSetList.add(chgSet);
    }

    public Integer getMaxChgSetSize() {
        return max(this.chgSetList);
    }

    public double getAvgChgSetSize() {
        double sum = 0;

        for(Integer x : chgSetList) {
            sum += x;
        }

        double avg = sum/chgSetList.size();

        if(Double.isNaN(avg)) return 0;

        return avg;
    }

    public void addFix() {
        this.fixes++;
    }

    public int getFixes() {
        return this.fixes;
    }

    public void setInsertDate(ZonedDateTime insertionDate) {
        this.insertDate = insertionDate;
    }

    public ZonedDateTime getInsertDate() {
        return this.insertDate;
    }
}
