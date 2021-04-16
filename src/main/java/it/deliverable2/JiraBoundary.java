package it.deliverable2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;

public class JiraBoundary {
    private List<Release> releases;

    public JiraBoundary(List<Release> releases) {
        this.releases = releases;
    }

    private String findRelease(ZonedDateTime dateTime) {
        Release prev = releases.get(0);

        for(Release rel : releases) {
            if(dateTime.isAfter(rel.getDate())) {
                return prev.getName();
            }

            prev = rel;
        }

        return "";
    }

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream();
             BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        ) {
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    public List<Issue> getBugs(String projName) throws IOException {
        List<Issue> issuesList = new ArrayList<>();
        int i = 0;
        int j;
        int total;
        do {
            j = i + 1000;

            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=" + projName + "%20AND%20issuetype=Bug%20AND%20status%20=%20Closed%20and%20resolution%20=%20fixed" +
                    "%20AND%20affectedVersion%20in%20releasedVersions()&startAt=" + i + "&maxResults=" + j;

            JSONObject json = this.readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");

            for (; i < total && i < j; i++) {
                JSONObject jsonObject = issues.getJSONObject(i);
                JSONObject fieldsObject = jsonObject.getJSONObject("fields");
                //Get affected versions
                JSONArray affectedVersionArray = fieldsObject.getJSONArray("versions");
                JSONArray fixVersionArray = fieldsObject.getJSONArray("fixVersions");

                List<String> affectedVersions = new ArrayList<>();
                List<String> fixedVersion = new ArrayList<>();

                //Get affected versions
                for(int x = 0; x < affectedVersionArray.length(); x++) {
                    String version = affectedVersionArray.getJSONObject(x).getString("name");
                    affectedVersions.add(version);
                }

                //Get fixed versions
                for(int x = 0; x < fixVersionArray.length(); x++) {
                    String version = fixVersionArray.getJSONObject(x).getString("name");
                    fixedVersion.add(version);
                }

                String dateString = fieldsObject.getString("resolutiondate");

                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .appendPattern("XX")
                        .toFormatter();

                ZonedDateTime resolutionDate = ZonedDateTime.parse(dateString, formatter);

                //Use date to find fixed version
                if(fixedVersion.isEmpty()) {
                    fixedVersion.add(findRelease(resolutionDate));
                }

                //Do not consider issue with same injected and fixed version
                if(affectedVersions.size() == fixedVersion.size() && affectedVersions.get(0).equals(fixedVersion.get(0))) continue;

                Issue issue = new Issue(jsonObject.getString("key"), affectedVersions, fixedVersion, resolutionDate);
                issuesList.add(issue);
            }
        } while (i < total);

        return issuesList;
    }
}
