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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubBoundary {

    private static final String COMMIT_STRING = "commit";
    private static final String GITHUB_STRING = "https://api.github.com/repos/";
    private static final Logger LOGGER = Logger.getLogger( GitHubBoundary.class.getName() );
    private double firstPercentReleases = 0.5;
    //Runtime for console commands
    private final Runtime runtime = Runtime.getRuntime();
    private String projOwner = "";
    private String projName = "";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ZZ");

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
            LOGGER.log(Level.SEVERE, "Unable to open git_token");
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
        List<CommitFile> fileList = new ArrayList<>();

        for(int i = 0; i < fileArray.length(); i++) {
            JSONObject obj = fileArray.getJSONObject(i);
            fileList.add(new CommitFile(obj));
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
            String url = GITHUB_STRING + projOwner + "/" + projName + "/tags?per_page=100&page=" + page + "&include_all_branches=true";
            jsonReleases = readJsonArrayFromUrl(url);

            for(int i = 0; i < jsonReleases.length(); i++) {
                JSONObject obj = (JSONObject) jsonReleases.get(i);

                //If the release is a release candidate do not consider it
                String name = obj.getString("name");

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
            String url = GITHUB_STRING + projOwner + "/" + projName + "/commits?per_page=100&page=" + page + "&include_all_branches=true";
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
        String url = GITHUB_STRING + this.projOwner + "/" + this.projName +"/commits/" + sha;

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

    public List<ReleaseFile> getReleaseFileList(Release rel, File localPath) throws IOException {
        List<ReleaseFile> releaseFileList = new ArrayList<>();

            Process process = runtime.exec("git ls-tree -r " + rel.getFullName() + " --name-only", null, localPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            LOGGER.log(Level.INFO, "Checking {0} files", rel.getName());
            //Track only .java files
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(".java")) {
                    releaseFileList.add(new ReleaseFile(line));
                }
            }
        return releaseFileList;
    }

    private void setTouchedFile(List<Commit> commitList, Map<String, ReleaseFile> fileMap) {
        for(Commit commit : commitList) {
            List<CommitFile> touchedFiles = commit.getTouchedFiles();

            for(CommitFile file : touchedFiles) {
                String filename = file.getFilename();
                if(fileMap.containsKey(filename)) {
                    ReleaseFile releaseFile = fileMap.get(filename);

                    releaseFile.addAddedAndDelition(file.getAddition(), file.getDeletion());
                    releaseFile.addAuthor(commit.getAuthor());

                    int chgSetSize = touchedFiles.size() - 1;
                    if(chgSetSize < 0) chgSetSize = 0;

                    releaseFile.addChgSetSize(chgSetSize);
                    releaseFile.addRevision();
                }
            }
        }
    }

    //Set insertion dates for all file
    public ZonedDateTime setInsertionDate(ReleaseFile releaseFile, File localPath, Map<String, ZonedDateTime> insertedMap) throws IOException {

            String filename = releaseFile.getFilename();
            if (insertedMap.containsKey(filename)) {
                releaseFile.setInsertDate(insertedMap.get(filename));
                return insertedMap.get(filename);
            }

            Process process = runtime.exec("git log --diff-filter=A --pretty=format:%ci -- " + filename, null, localPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            ZonedDateTime dateTime;

            String line;
            while ((line = reader.readLine()) != null) {

                if (line.length() > 0) {
                    dateTime = ZonedDateTime.parse(line, formatter);

                    insertedMap.put(filename, dateTime);
                    return dateTime;
                }
            }
        return insertedMap.get(filename);
    }

    //Set metrics and assign files to releases
    public void assignFilesToReleases(List<Release> releases, List<Commit> commitList, File localPath) throws IOException {
        Map<String, ZonedDateTime> insertDateMap = new HashMap<>();

        for(Release rel : releases) {
            Map<String, ReleaseFile> fileMap = new HashMap<>();

            //Fetch the release file list
            List<ReleaseFile> releaseFileList = getReleaseFileList(rel, localPath);

            //Get all release files
            for(ReleaseFile relFile : releaseFileList) {
                fileMap.put(relFile.getFilename(), relFile);
                relFile.setInsertDate(setInsertionDate(relFile, localPath, insertDateMap));
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

            //Set metrics
            setTouchedFile(commitsOfRelease, fileMap);

            //Set lines of code
            for(ReleaseFile releaseFile : fileMap.values()) {
                releaseFile.setLoc(getLinesOfCode(rel, releaseFile.getFilename(), localPath));
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
                        List<CommitFile> commitFileList = commit.getTouchedFiles();

                        issue.setAffects(commitFileList);
                    }
                }
            }
        }
    }

    //Set the affected file of an issue list
    public void setIssueAffectFile(List<Issue> issues, Map<String, List<Commit>> commitMap) {
        for(Issue issue : issues) {
            LOGGER.log(Level.INFO, "Doing {0}\r", issue.getName());
            String key = issue.getName();

            if(commitMap.containsKey(key)) {
                List<Commit> commits = commitMap.get(key);

                for(Commit commit : commits) {
                    issue.setAffects(commit.getTouchedFiles());
                }
            }
        }
    }

    //Get date of a release
    public ZonedDateTime getReleaseDate(String releaseName, File localPath) throws IOException {
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

    private String findKey(String message) {
        Pattern patternInitial = Pattern.compile("((?<=(ISSUE|AVRO|BOOKKEEPER)-)[0-9]+|(?<=(ISSUE|AVRO|BOOKKEPER) #)[0-9]+)", Pattern.CASE_INSENSITIVE);
        Pattern patterFinal = Pattern.compile("(?<=.\\(#)[0-9]+(?=\\))", Pattern.CASE_INSENSITIVE);
        Matcher matcherInitial = patternInitial.matcher(message);
        Matcher matcherFinal = patterFinal.matcher(message);

        String key;
        String name;

        if (matcherInitial.find()) {
            key = matcherInitial.group(0);
            name = projName.toUpperCase() + "-"+ key;
        } else if(matcherFinal.find()) {
            key = matcherFinal.group(0);
            name = projName.toUpperCase() + "-"+ key;
        } else {
            name = message;
        }

        return name;
    }

    //Get commits with touched files
    public List<Commit> getCommits(File localPath) throws IOException {
        List<Commit> commitList = new ArrayList<>();

        Process process = runtime.exec("git log --numstat --pretty=format:Commit###%H###%ci###%an###%s", null, localPath);
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

                //Ignore merge commits
                if(message.startsWith("Merge")) continue;

                String name;

                ZonedDateTime dateTime = ZonedDateTime.parse(dateString, formatter);

                name = findKey(message);

                Commit commit = new Commit(name, message, sha, dateTime);
                commit.setAuthor(author);

                prevCommit = commit;
                commitList.add(commit);
            } else {
                if(!line.isEmpty() && line.contains(".java")) {
                    String[] tokens = line.split("[\t]");

                    int added = Integer.parseInt(tokens[0]);
                    int deleted = Integer.parseInt(tokens[1]);
                    String filename = tokens[2];

                    CommitFile commitFile = new CommitFile(filename, added, deleted);
                    prevCommit.addRepoFile(commitFile);
                }
            }
        }

        //Order commits
        Collections.sort(commitList);

        return commitList;
    }

    //Get non merged branches that maintain older versions
    private List<String> getNonMergedBranches(File localPath) throws IOException {
        Process process = runtime.exec("git branch -r --no-merge", null, localPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        List<String> branchList = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("  origin/branch-")) {
                branchList.add(line);
            }
        }

        return branchList;
    }

    //Get the version of the fixed ticket
    public Map<String, Release> getBranchFixedTickets(List<Release> releases, File localPath) throws IOException {
        List<String> branchList = getNonMergedBranches(localPath);
        Map<String, Release> ticketMap = new HashMap<>();

        for(String branch : branchList) {
            String branchName = branch.split("-")[1];

            Release release = null;

            //Find Release of the maintaned branch
            for(Release rel : releases) {
                String relName = rel.getName();
                if (relName.startsWith(branchName)) {
                    release = rel;
                }
            }

            //Log commit since the release branch
            if(release == null) {
                continue;
            }

            Process process = runtime.exec("git log --pretty=format:%s " + branch + " --since=" + release.getDate(), null, localPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while((line = reader.readLine()) != null) {
                String name = getCommitName(line);

                //If there is a ticket put the fix version in the map
                if(!name.equals(line)) {
                    ticketMap.put(name, release);
                }
            }
        }

        return ticketMap;
    }
}
