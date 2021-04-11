package it.deliverable2;

import org.json.JSONObject;

import java.time.ZonedDateTime;

public class Commit {
    private String name;
    private String message;

    //Identifier of the commit
    private String sha;
    private String author;
    private String commitUrl;
    private JSONObject jsonObject;

    //Commit Date
    private ZonedDateTime date;

    //Edited Files
    private String[] files;

    public Commit() {
    }

    public Commit(String name, String message, String sha, ZonedDateTime date) {
        this.name = name;
        this.message = message;
        this.sha = sha;
        this.date = date;
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

    public String[] getFiles() {
        return files;
    }

    public void setFiles(String[] files) {
        this.files = files;
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


    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

}
