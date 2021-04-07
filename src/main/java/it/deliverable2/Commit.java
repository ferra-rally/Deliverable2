package it.deliverable2;

import java.time.ZonedDateTime;

public class Commit {
    private String name;
    private String message;
    private String sha;
    private String author;

    //Commit Date
    private ZonedDateTime date;

    //Edited Files
    private String[] files;

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
}
