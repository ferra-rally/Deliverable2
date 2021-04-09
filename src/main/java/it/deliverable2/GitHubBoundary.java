package it.deliverable2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class GitHubBoundary {

    private static final String COMMIT_STRING = "commit";

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private String getCommitName(String message) {
        String name;
        if(message.contains(":")) {
            name = message.split(": ")[0];
        } else {
            name = message;
        }

        return name;
    }

    public String getGitHubToken() {
        String tokenLocation = "github_deliverable_token";
        File file = new File(tokenLocation);

        try (BufferedReader brTest = new BufferedReader(new FileReader(file))) {
            return brTest.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public JSONObject readJsonFromUrlGitHub(String url) throws IOException, JSONException {
        URL url1 = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) url1.openConnection();

        conn.setRequestProperty("Authorization", "token " + this.getGitHubToken());

        try (InputStream is = conn.getInputStream();
             BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
        ) {
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    public JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream(); BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String jsonText = readAll(rd);

            return new JSONArray(jsonText);
        }
    }

    public Commit getCommitFromUrl(String url) throws IOException {
        JSONObject jsonCommit = this.readJsonFromUrlGitHub(url);

        JSONObject commitJSONObject = jsonCommit.getJSONObject(COMMIT_STRING);

        String message = commitJSONObject.getString("message");
        String name;

        name = getCommitName(message);

        ZonedDateTime dateTime = ZonedDateTime.parse(commitJSONObject.getJSONObject("committer").getString("date"));

        System.out.println(jsonCommit.getString("sha") + " " + name + " " + dateTime);

        return new Commit(name, message, jsonCommit.getString("sha"), dateTime);
    }

    public List<Release> getReleases(String projOwner, String projName) throws IOException {
        List<Release> releases = new ArrayList<>();
        int page = 1;
        JSONArray jsonReleases;

        do {
            String url = "https://api.github.com/repos/" + projOwner + "/" + projName + "/tags?per_page=100&page=" + page;
            jsonReleases = readJsonArrayFromUrl(url);

            for(int i = 0; i < jsonReleases.length(); i++) {
                JSONObject obj = (JSONObject) jsonReleases.get(i);
                String commitUrl = obj.getJSONObject(COMMIT_STRING).getString("url");


                Release release = new Release(obj.getJSONObject(COMMIT_STRING).getString("url"), obj.getString("name"));
                Commit commit = getCommitFromUrl(commitUrl);
                release.setCommit(commit);

                release.setDate(commit.getDate());
                releases.add(release);
            }

            page++;

        } while(jsonReleases.length() > 0);

        for(int i = 0; i < releases.size(); i++) {
            releases.get(i).setNumber(releases.size() - i);
        }

        return releases;
    }

    public List<Commit> getCommits(String projOwner, String projName) throws IOException {
        List<Commit> commits = new ArrayList<>();
        int page = 1;
        JSONArray jsonCommits;

        do {
            String url = "https://api.github.com/repos/" + projOwner + "/" + projName + "/commits?per_page=100&page=" + page;
            jsonCommits = readJsonArrayFromUrl(url);

            for(int i = 0; i < jsonCommits.length(); i++) {
                JSONObject obj = (JSONObject) jsonCommits.get(i);

                Commit commit = new Commit();
                commit.setAuthor(obj.getJSONObject(COMMIT_STRING).getJSONObject("author").getString("name"));

                String message = obj.getJSONObject(COMMIT_STRING).getString("message");
                commit.setMessage(message);
                commit.setName(getCommitName(message));

                commits.add(commit);
            }

            System.out.println("Doing page " + page + " with " + jsonCommits.length() + " releases");
            page++;

        } while(jsonCommits.length() > 0);

        return commits;
    }
}
