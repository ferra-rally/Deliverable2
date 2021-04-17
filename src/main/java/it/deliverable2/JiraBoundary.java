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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JiraBoundary {
    private static final Logger LOGGER = Logger.getLogger(JiraBoundary.class.getName());

    private Release findRelease(ZonedDateTime dateTime, List<Release> allReleases) {
        Release prev = allReleases.get(0);

        for (Release rel : allReleases) {
            if (dateTime.isAfter(rel.getDate())) {
                return prev;
            }

            prev = rel;
        }

        return prev;
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

    public List<Issue> getBugs(String projName, List<Release> allReleases) throws IOException {
        List<Issue> issuesList = new ArrayList<>();

        //Create a map with all releases
        Map<String , Release> releaseMap = new HashMap<>();
        for(Release rel : allReleases) {
            releaseMap.put(rel.getName(), rel);
        }

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

                List<Release> affectedVersions = new ArrayList<>();
                List<Release> fixedVersion = new ArrayList<>();

                //Get affected versions
                for (int x = 0; x < affectedVersionArray.length(); x++) {
                    String version = affectedVersionArray.getJSONObject(x).getString("name");
                    affectedVersions.add(releaseMap.get(version));
                }

                //Get fixed versions
                for (int x = 0; x < fixVersionArray.length(); x++) {
                    String version = fixVersionArray.getJSONObject(x).getString("name");
                    fixedVersion.add(releaseMap.get(version));
                }

                String dateString = fieldsObject.getString("resolutiondate");

                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .appendPattern("XX")
                        .toFormatter();

                ZonedDateTime resolutionDate = ZonedDateTime.parse(dateString, formatter);

                //Use date to find fixed version
                if (fixedVersion.isEmpty()) {
                    fixedVersion.add(findRelease(resolutionDate, allReleases));
                }

                //Do not consider issue with same injected and fixed version
                if (affectedVersions.size() == fixedVersion.size() && affectedVersions.get(0).equals(fixedVersion.get(0)))
                    continue;

                Issue issue = new Issue(jsonObject.getString("key"), affectedVersions, fixedVersion, resolutionDate);
                issuesList.add(issue);
            }
        } while (i < total);

        return issuesList;
    }

    public List<Release> getFirstPercentOfReleases(List<Release> releases, double firstPercentReleases) {
        List<Release> croppedReleasesList = new ArrayList<>();

        int number = (int) Math.floor(releases.size() * (1 - firstPercentReleases));

        for (int i = 0; i < number; i++) {
            croppedReleasesList.add(releases.get(i));
        }

        List<String> releaseNames = new ArrayList<>();
        for (Release rel : croppedReleasesList) {
            releaseNames.add(rel.getName());
        }
        LOGGER.log(Level.INFO, "Used releases {0}", releaseNames);

        return croppedReleasesList;
    }

    public List<Release> getReleases(String projName, File localPath) throws IOException {
        List<Release> releasesList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName.toUpperCase(Locale.ROOT);

        JSONObject result = this.readJsonFromUrl(url);
        JSONArray versions = result.getJSONArray("versions");

        for (int i = 0; i < versions.length(); i++) {
            JSONObject versionJSON = versions.getJSONObject(i);
            String name = versionJSON.getString("name");
            ZonedDateTime zonedDateTime;

            if (versionJSON.isNull("releaseDate")) {
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

        //Order releases
        Collections.sort(releasesList);

        for (int i = 0; i < releasesList.size(); i++) {
            Release rel = releasesList.get(i);
            rel.setNumber(i + 1);
        }

        //DEBUG show all releases and used releases
        List<String> releaseNames = new ArrayList<>();
        for (Release rel : releasesList) {
            releaseNames.add(rel.getName());
        }
        LOGGER.log(Level.INFO, "All releases {0}", releaseNames);

        return releasesList;
    }

}
