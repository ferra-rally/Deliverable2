package it.deliverable2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitHubBoundary {

    private static final String COMMIT_STRING = "commit";
    private static final Logger LOGGER = Logger.getLogger( GitHubBoundary.class.getName() );
    private double firstPercentReleases = 0.5;
    //Runtime for console commands
    private final Runtime runtime = Runtime.getRuntime();
    private String projOwner = "";
    private String projName = "";

    public GitHubBoundary(String projOwner, String projName, double firstPercentReleases) {
        this.projOwner = projOwner;
        this.projName = projName;
        this.firstPercentReleases = firstPercentReleases;
    }

    public GitHubBoundary() {

    }

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
        if(message.startsWith(this.projName.toUpperCase(Locale.ROOT))) {
            //Split names
            name = message.split("[: .]")[0];
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

        JSONArray fileArray = jsonCommit.getJSONArray("files");
        List<RepoFile> fileList = new ArrayList<>();

        for(int i = 0; i < fileArray.length(); i++) {
            JSONObject obj = fileArray.getJSONObject(i);
            fileList.add(new RepoFile(obj));
        }

        ZonedDateTime dateTime = ZonedDateTime.parse(commitJSONObject.getJSONObject("committer").getString("date"));
        Commit commit = new Commit(name, message, jsonCommit.getString("sha"), dateTime);
        commit.setTouchedFiles(fileList);
        return commit;
    }

    public List<Release> getReleases() throws IOException {
        List<Release> releases = new ArrayList<>();
        List<Release> finalReleases = new ArrayList<>();

        int page = 1;
        JSONArray jsonReleases;

        do {
            String url = "https://api.github.com/repos/" + projOwner + "/" + projName + "/tags?per_page=100&page=" + page + "&include_all_branches=true";
            jsonReleases = readJsonArrayFromUrl(url);

            for(int i = 0; i < jsonReleases.length(); i++) {
                JSONObject obj = (JSONObject) jsonReleases.get(i);

                //If the release is a release candidate do not consider it
                String name = obj.getString("name");

                //TODO do not consider extra releases
                //For bookeper ignore docker
                if(name.contains("rc")) {
                    continue;
                }

                Release release = new Release(name, obj.getJSONObject(COMMIT_STRING).getString("url"));
                release.setFullName(name);

                releases.add(release);
            }

            LOGGER.log(Level.INFO, "Releases: Doing page {0} with {1} releases", new Object[]{page, jsonReleases.length()});
            page++;

        } while(jsonReleases.length() > 0);

        for(int i = 0; i < releases.size(); i++) {
            Release rel = releases.get(i);
            rel.setNumber(releases.size() - i);
        }

        int number = (int) Math.ceil(releases.size() * (1 - firstPercentReleases));

        //Invert order and set commit
        for (int i = releases.size() - 1; i >= number; i--) {
            Release rel = releases.get(i);
            Commit commit = getCommitFromUrl(rel.getCommitUrl());
            rel.setCommit(commit);

            rel.setDate(commit.getDate());
            finalReleases.add(rel);
        }

        //DEBUG show all releases and used releases
        List<String> releaseNames = new ArrayList<>();
        for(Release rel : releases) {
            releaseNames.add(rel.getName());
        }
        LOGGER.log(Level.INFO, "All releases {0}", releaseNames);

        releaseNames = new ArrayList<>();
        for(Release rel : finalReleases) {
            releaseNames.add(rel.getName());
        }
        LOGGER.log(Level.INFO, "Used releases {0}", releaseNames);

        Collections.sort(finalReleases);
        return finalReleases;
    }

    public List<Commit> getCommits() throws IOException {
        List<Commit> commits = new ArrayList<>();
        int page = 1;
        JSONArray jsonCommits;

        do {
            String url = "https://api.github.com/repos/" + projOwner + "/" + projName + "/commits?per_page=100&page=" + page + "&include_all_branches=true";
            jsonCommits = readJsonArrayFromUrl(url);

            for(int i = 0; i < jsonCommits.length(); i++) {
                JSONObject obj = (JSONObject) jsonCommits.get(i);
                JSONObject commitObj = obj.getJSONObject(COMMIT_STRING);

                Commit commit = new Commit();
                commit.setAuthor(commitObj.getJSONObject("author").getString("name"));

                String message = commitObj.getString("message");
                commit.setMessage(message);
                commit.setName(getCommitName(message));

                ZonedDateTime dateTime = ZonedDateTime.parse(commitObj.getJSONObject("committer").getString("date"));
                commit.setDate(dateTime);
                commit.setCommitUrl(obj.getString("url"));
                commit.setSha(obj.getString("sha"));

                commits.add(commit);
            }

            LOGGER.log(Level.INFO, "Commits: Doing page {0} with {1} commits", new Object[]{page, jsonCommits.length()});

            page++;

        } while(jsonCommits.length() > 0);

        Collections.reverse(commits);
        return commits;
    }

    public Commit searchCommit(String name) throws IOException {
        Commit commit = new Commit();

        URL url = new URL("https://api.github.com/search/commits?q=repo:" + projOwner + "/" + projName + "+" + '"' + name + '"');

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestProperty("Authorization", "token " + this.getGitHubToken());
        conn.setRequestProperty("Accept", "application/vnd.github.cloak-preview");

        try (InputStream is = conn.getInputStream();
             BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
        ) {
            String jsonText = readAll(rd);

            JSONObject searchResults =  new JSONObject(jsonText);
            //There can be multiple search results
            int total = searchResults.getInt("total_count");
            if(total <= 0) {
                LOGGER.log(Level.WARNING, "Commit {0} not found ", name);
            } else if(total == 1) {
                LOGGER.log(Level.WARNING, "Commit {0} found", name);
                JSONObject result = searchResults.getJSONArray("items").getJSONObject(0);
                String commitUrl = result.getString("url");

                try {
                    commit = this.getCommitFromUrl(commitUrl);
                } catch (IOException e) {
                    LOGGER.log(Level.INFO, "Commit {0} did not edit files", name);
                }

            } else {
                LOGGER.log(Level.WARNING, "Multiple results found for commit {0}", name);
            }
        }

        return commit;
    }

    public Commit getCommitFromSha(String sha) throws IOException {
        String url = "https://api.github.com/repos/" + this.projOwner + "/" + this.projName +"/commits/" + sha;

        return this.getCommitFromUrl(url);
    }

    public int getLinesOfCode(Release release, String filename, File localPath) throws IOException {

        Process process = runtime.exec("git show " + release.getFullName() + ":" + filename, null, localPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder stringBuilder = new StringBuilder();
        String line;

        while((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }

        String[] lines = stringBuilder.toString().split("\r\n|\r|\n");

        return lines.length;
    }

    public List<String> getReleaseFileList(Release rel, File localPath) throws IOException {
        List<String> releaseFileList = new ArrayList<>();

            Process process = runtime.exec("git ls-tree -r " + rel.getFullName() + " --name-only", null, localPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            LOGGER.log(Level.INFO, "Checking {0} files", rel.getName());
            //Track only .java files
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(".java")) {
                    releaseFileList.add(line);
                }
            }


        return releaseFileList;
    }

    private void setTouchedFile(List<Commit> commitList, Map<String, RepoFile> fileMap) {
        for(Commit commit : commitList) {
            List<RepoFile> touchedFiles = commit.getTouchedFiles();

            for(RepoFile file : touchedFiles) {
                String filename = file.getFilename();
                if(fileMap.containsKey(filename)) {
                    RepoFile repoFile = fileMap.get(filename);

                    repoFile.addAdded(file.getAddition());
                    repoFile.addDeletion(file.getDeletion());
                    repoFile.addAuthor(commit.getAuthor());
                    repoFile.addRevision();
                }
            }
        }
    }

    //Assign files to release
    public void assignFilesToReleases(List<Release> releases, List<Commit> commitList, File localPath) throws IOException {
        for(Release rel : releases) {

            Map<String, RepoFile> fileMap = new HashMap<>();

            //Get all release files
            for(String filename : getReleaseFileList(rel, localPath)) {
                fileMap.put(filename, new RepoFile(filename));
            }

            List<Commit> commitsOfRelease = new ArrayList<>();

            //Assign commits to releases
            for(Iterator<Commit> iterator = commitList.iterator(); iterator.hasNext(); ) {
                Commit commit = iterator.next();
                if (commit.getDate().isBefore(rel.getDate())) {
                    commitsOfRelease.add(commit);
                    iterator.remove();
                }
            }

            //Set addition, deletion and authors
            setTouchedFile(commitsOfRelease, fileMap);

            //Set lines of code
            for(RepoFile repoFile : fileMap.values()) {
                repoFile.setLoc(getLinesOfCode(rel, repoFile.getFilename(), localPath));
            }

            rel.setCommits(commitsOfRelease);
            rel.setFileMap(fileMap);
            LOGGER.log(Level.INFO, "Release {0} has {1} commits", new Object[]{rel.getName(), commitsOfRelease.size()});
        }
    }

    public void setIssueAffectFile(List<Issue> issues, File localPath) throws IOException {
        for (Issue issue : issues) {
            LOGGER.log(Level.INFO, "Doing {0}\r", issue.getName());
            Process process = runtime.exec("git log --all --grep=\"" + issue.getName() + "[: .]\"", null, localPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(COMMIT_STRING)) {
                    String sha = line.split(" ")[1];
                    if (!sha.isEmpty()) {
                        Commit commit = getCommitFromSha(sha);
                        List<RepoFile> repoFileList = commit.getTouchedFiles();

                        issue.setAffects(repoFileList);
                    }
                }
            }
        }
    }

    public ZonedDateTime getReleaseDate(String releaseName, File localPath) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ZZ");

        Process process = runtime.exec("git log -1 --format=%ai release-" + releaseName, null, localPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        ZonedDateTime dateTime;

        String line;
        while ((line = reader.readLine()) != null) {

            if(line.length() > 0) {
                dateTime = ZonedDateTime.parse(line, formatter);
                return dateTime;
            }
        }

        return ZonedDateTime.now();
    }

    public List<Commit> getCommits(ZonedDateTime untilDate, File localPath) throws IOException {
        List<Commit> commitList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ZZ");

        Process process = runtime.exec("git log --numstat --pretty=format:Commit###%H###%ci###%an###%s --before=" + untilDate, null, localPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        Commit prevCommit = new Commit();

        String line;
        while((line = reader.readLine()) != null) {
            if(line.startsWith("Commit")) {
                String[] tokens = line.split("###");

                String sha = tokens[1];
                String dateString = tokens[2];
                String author = tokens[3];
                String message = tokens[4];

                ZonedDateTime dateTime = ZonedDateTime.parse(dateString, formatter);

                Commit commit = new Commit(getCommitName(message), message, sha, dateTime);
                commit.setAuthor(author);

                prevCommit = commit;
                commitList.add(commit);
            } else {
                if(!line.isEmpty() && line.contains(".java")) {
                    String[] tokens = line.split("[\t]");

                    int added = Integer.parseInt(tokens[0]);
                    int deleted = Integer.parseInt(tokens[1]);
                    String filename = tokens[2];

                    RepoFile repoFile = new RepoFile(filename, added, deleted);
                    prevCommit.addRepoFile(repoFile);
                }
            }
        }

        //Order commits
        Collections.sort(commitList);

        return commitList;
    }
}
