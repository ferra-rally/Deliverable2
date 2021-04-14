package it.deliverable2;

import org.json.JSONObject;

import java.time.ZonedDateTime;
import java.util.List;

public class Commit {
    private String name;
    private String message;

    //Identifier of the commit
    private String sha;
    private String author;
    private String commitUrl;
    private JSONObject jsonObject;
    private List<RepoFile> repoFileList;
    private int additions;
    private int deletion;

    //Commit Date
    private ZonedDateTime date;

    public Commit() {
    }

    public Commit(String name, String message, String sha, ZonedDateTime date) {
        this.name = name;
        this.message = message;
        this.sha = sha;
        this.date = date;
    }

    public Commit(JSONObject jsonObject) {
        //TODO constructor using json
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
    }

    public List<RepoFile> getRepoFileList() {
        return repoFileList;
    }

    public void setRepoFileList(List<RepoFile> repoFileList) {
        this.repoFileList = repoFileList;
    }

    public int getAdditions() {
        return additions;
    }

    public void setAdditions(int additions) {
        this.additions = additions;
    }

    public int getDeletion() {
        return deletion;
    }

    public void setDeletion(int deletion) {
        this.deletion = deletion;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        /*
        this.jsonObject = jsonObject;

        this.setAdditions(jsonObject.getJSONObject("stats").getInt("additions"));
        this.setDeletion(jsonObject.getJSONObject("stats").getInt("deletions"));

        List<RepoFile> repoFileListTemp = new ArrayList<>();

        //TODO if does not exists?
        JSONArray filesArray = jsonObject.getJSONArray("files");

        for(int i = 0; i < filesArray.length(); i++) {
            JSONObject fileObject = filesArray.getJSONObject(i);

            String filename = fileObject.getString("filename");

            //Ignore files that are not java files
            if(!filename.contains(".java")) {
                continue;
            }
            RepoFile repoFile = new RepoFile(fileObject.getString("sha") , filename,
                    fileObject.getInt("additions"), fileObject.getInt("deletions"), fileObject.getInt("changes"));

            repoFileListTemp.add(repoFile);
        }

        this.setRepoFileList(repoFileListTemp);*/

        this.jsonObject = jsonObject;
    }

}
