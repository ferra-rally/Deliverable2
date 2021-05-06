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
    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .appendPattern("XX")
            .toFormatter();

    private Release findRelease(ZonedDateTime dateTime, List<Release> allReleases) {
        Collections.sort(allReleases);

        Release prev = allReleases.get(0);
        for (int i = 0; i < allReleases.size(); i++) {
            Release rel = allReleases.get(i);

            if (dateTime.isAfter(rel.getDate())) {
                return prev;
            } else if (i == allReleases.size() - 1) {
                return allReleases.get(allReleases.size() - 1);
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

    //Get the first affected version if there are multiple
    private Release findInjected(List<Release> affectedList) {
        Release injectVersion = affectedList.get(0);

        for (int i = 1; i < affectedList.size(); i++) {
            if (injectVersion.compareTo(affectedList.get(i)) > 0) injectVersion = affectedList.get(i);
        }


        return injectVersion;
    }

    private void addProportion(Release injected, Release opening, Release fixed, List<Double> proportionList) {
        int fixedNumber = fixed.getNumber();
        int injectedNumber = injected.getNumber();
        int openingNumber = opening.getNumber();

        //Invalid proportion
        if (openingNumber >= fixedNumber) {
            return;
        }

        double proportion = (fixedNumber * 1.0 - injectedNumber) / (fixedNumber - openingNumber);
        if (Double.isFinite(proportion) && proportion > 0) {

            int index = Collections.binarySearch(proportionList, proportion);
            if (index < 0) {
                index = -index - 1;
            }
            proportionList.add(index, proportion);
        }
    }

    private Release findInjectedVersionProportion(List<Double> proportionList, List<Release> releases, Release fixedVersion, Release openingVersion) {
        //Implements incremental proportion
        double proportion = proportionList.get((int) Math.floor((proportionList.size() * 1.0) / 2));

        int injectedNumber = (int) Math.floor(fixedVersion.getNumber() - ((fixedVersion.getNumber() - openingVersion.getNumber()) * proportion));

        //Release are ordered and have number = position + 1

        return releases.get(injectedNumber - 1);
    }

    private Release findFixed(String key, Map<String, List<Commit>> commitMap, JSONObject jsonObject, Map<String, Release> releaseMap, Map<String, Release> ticketMap) {
        JSONObject fieldsObject = jsonObject.getJSONObject("fields");
        JSONArray fixVersionArray = fieldsObject.getJSONArray("fixVersions");

        if(!fixVersionArray.isEmpty()) {
            //If the fix version is present in jira get the last one

            //Search the fix version in jira
            List<Release> fixedVersions = new ArrayList<>();

            //Get fixed versions
            for (int x = 0; x < fixVersionArray.length(); x++) {
                String version = fixVersionArray.getJSONObject(x).getString("name");
                fixedVersions.add(releaseMap.get(version));
            }

            Release fixVersion = fixedVersions.get(0);

            //If there are multiple fix version return the last
            for (int i = 1; i < fixedVersions.size(); i++) {
                if (fixVersion.compareTo(fixedVersions.get(i)) < 0) fixVersion = fixedVersions.get(i);
            }

            return fixVersion;

        } else {
            //There aren't fix versions in jira, use the date to get it

            //If the commit is available get the date and return the version
            if (commitMap.containsKey(key)) {
                //Get the last commit in order of date
                List<Commit> commitList = commitMap.get(key);

                Collections.sort(commitList);

                //Get the last commit
                ZonedDateTime fixDate = commitList.get(commitList.size() - 1).getDate();
                return findRelease(fixDate, new ArrayList<>(releaseMap.values()));
            } else if(ticketMap.containsKey(key)) {
                //Check if the ticket is fixed in a branch
                return ticketMap.get(key);
            } else {
                //If commit is not available use jira date
                String dateString = fieldsObject.getString("resolutiondate");

                ZonedDateTime fixDate =  ZonedDateTime.parse(dateString, formatter);
                return findRelease(fixDate, new ArrayList<>(releaseMap.values()));
            }
        }
    }

    public List<Issue> getBugs(String projName, List<Release> allReleases, Map<String, List<Commit>> commitMap, Map<String, Release> ticketMap) throws IOException {
        List<Issue> issuesList = new ArrayList<>();
        List<Double> proportionList = new ArrayList<>();

        //TODO use 0.5?
        proportionList.add(0.5);

        //Create a map with all releases
        Map<String, Release> releaseMap = new HashMap<>();
        for (Release rel : allReleases) {
            releaseMap.put(rel.getName(), rel);
        }

        int i = 0;
        int j;
        int total;

        do {
            j = i + 1000;

            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=" + projName +
                    "%20AND%20issuetype=Bug%20AND%20(status%20=%20Closed%20OR%20status%20=%20Resolved)%20and%20resolution%20=%20fixed" +
                    "%20ORDER%20BY%20createdDate%20ASC%20&startAt=" + i + "&maxResults=" + j;

            JSONObject json = this.readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");

            for (; i < total && i < j; i++) {
                JSONObject jsonObject = issues.getJSONObject(i);
                JSONObject fieldsObject = jsonObject.getJSONObject("fields");
                //Get affected versions
                JSONArray affectedVersionArray = fieldsObject.getJSONArray("versions");
                String key = jsonObject.getString("key");

                List<Release> affectedVersions = new ArrayList<>();

                String dateString = fieldsObject.getString("created");
                ZonedDateTime creationDate = ZonedDateTime.parse(dateString, formatter);

                Release openingVersion = findRelease(creationDate, allReleases);

                Release fixed = findFixed(key, commitMap, jsonObject, releaseMap, ticketMap);
                ZonedDateTime resolutionDate = fixed.getDate();

                Release injected;

                if (affectedVersionArray.length() == 0) {
                    //Use proportion
                    injected = findInjectedVersionProportion(proportionList, allReleases, fixed, openingVersion);
                } else {

                    //Get affected versions
                    for (int x = 0; x < affectedVersionArray.length(); x++) {
                        String version = affectedVersionArray.getJSONObject(x).getString("name");
                        affectedVersions.add(releaseMap.get(version));
                    }

                    injected = findInjected(affectedVersions);
                    addProportion(injected, openingVersion, fixed, proportionList);
                }

                Issue issue = new Issue(key, injected, fixed, openingVersion, resolutionDate);
                issuesList.add(issue);
            }
        } while (i < total);

        return issuesList;
    }

    //Get releases from Jira
    public List<Release> getReleases(String projName, File localPath) throws IOException {
        List<Release> releasesList = new ArrayList<>();
        DateTimeFormatter formatterRelease = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName.toUpperCase(Locale.ROOT);

        JSONObject result = this.readJsonFromUrl(url);
        JSONArray versions = result.getJSONArray("versions");

        for (int i = 0; i < versions.length(); i++) {
            JSONObject versionJSON = versions.getJSONObject(i);
            String name = versionJSON.getString("name");
            ZonedDateTime zonedDateTime;

            //If release date is not present in JIRA get it from GitHUb
            if (versionJSON.isNull("releaseDate")) {
                zonedDateTime = new GitHubBoundary().getReleaseDate(name, localPath);
            } else {
                String dateString = versionJSON.getString("releaseDate");
                LocalDate date = LocalDate.parse(dateString, formatterRelease);

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
