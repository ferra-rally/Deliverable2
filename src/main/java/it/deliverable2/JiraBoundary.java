package it.deliverable2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JiraBoundary {
    private List<Release> releases;
    private List<Release> allReleases;

    private static final Logger LOGGER = Logger.getLogger( JiraBoundary.class.getName() );

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

    public List<Release> getReleases(String projName, double firstPercentReleases, File localPath) throws IOException {
        List<Release> releasesList = new ArrayList<>();
        List<Release> croppedReleasesList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName.toUpperCase(Locale.ROOT);

        JSONObject result = this.readJsonFromUrl(url);
        JSONArray versions = result.getJSONArray("versions");

        for(int i = 0; i < versions.length(); i++) {
            JSONObject versionJSON = versions.getJSONObject(i);
            String name = versionJSON.getString("name");
            ZonedDateTime zonedDateTime;

            if(versionJSON.isNull("releaseDate")) {
                zonedDateTime = new GitHubBoundary().getReleaseDate(name, localPath);
            } else {
                String dateString = versionJSON.getString("releaseDate");
                LocalDate date = LocalDate.parse(dateString, formatter);

                //Missing timezone information, default to Greenwich
                zonedDateTime = date.atStartOfDay(ZoneId.of("UTC"));
            }

            Release release = new Release(name, zonedDateTime);
            releasesList.add(release);
        }

        Collections.sort(releasesList);

        for(int i = 0; i < releasesList.size(); i++) {
            releasesList.get(i).setNumber(i + 1);
        }

        int number = (int) Math.floor(releasesList.size() * (1 - firstPercentReleases));

        for(int i = 0; i < number; i++) {
            croppedReleasesList.add(releasesList.get(i));
        }

        //DEBUG show all releases and used releases
        List<String> releaseNames = new ArrayList<>();
        for(Release rel : releasesList) {
            releaseNames.add(rel.getName());
        }
        LOGGER.log(Level.INFO, "All releases {0}", releaseNames);

        releaseNames = new ArrayList<>();
        for(Release rel : croppedReleasesList) {
            releaseNames.add(rel.getName());
        }
        LOGGER.log(Level.INFO, "Used releases {0}", releaseNames);

        this.releases = croppedReleasesList;
        this.allReleases = releasesList;

        return croppedReleasesList;
    }

}
