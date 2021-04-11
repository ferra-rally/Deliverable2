package it.deliverable2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JiraBoundary {

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try (
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        )
        {
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        } finally {
            is.close();
        }
    }

    public List<Issue> getBugs(String projName) throws IOException {
        List<Issue> issuesList = new ArrayList<>();

        String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project="+ projName + "%20AND%20issuetype=Bug%20AND%20status%20=%20Closed%20and%20resolution%20=%20fixed" +
                "%20AND%20affectedVersion%20in%20releasedVersions()";
        JSONObject json = this.readJsonFromUrl(url);
        JSONArray issues = json.getJSONArray("issues");

        for(int i = 0; i < issues.length(); i++) {
            JSONObject jsonObject = issues.getJSONObject(i);
            JSONArray versionObject = jsonObject.getJSONObject("fields").getJSONArray("versions");
            String start;
            String end;
            if(versionObject.length() == 1) {
                end = start = versionObject.getJSONObject(0).getString("name");
            } else {
                start = versionObject.getJSONObject(0).getString("name");
                end = versionObject.getJSONObject(1).getString("name");
            }

            Issue issue = new Issue(jsonObject.getString("key"), start, end);
            issuesList.add(issue);
        }

        return issuesList;
    }
}
