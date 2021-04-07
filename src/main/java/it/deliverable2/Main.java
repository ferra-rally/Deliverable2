package it.deliverable2;

import org.eclipse.jgit.api.errors.GitAPIException;
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

public class Main {
    private static String AVRO_URL = "https://github.com/apache/avro.git";

    //Delete directory if exists
    private static boolean deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directory.delete();
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException{
        //InputStream is = new URL(url).openStream();

        URL url1 = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) url1.openConnection();

        conn.setRequestProperty("Authorization", "token " + System.getenv("GITHUB_TOKEN"));

        InputStream is = conn.getInputStream();

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

    public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));){
            String jsonText = readAll(rd);

            return new JSONArray(jsonText);
        } finally {
            is.close();
        }
    }

    public static Commit getCommitFromUrl(String url) throws IOException {
        JSONObject jsonCommit = readJsonFromUrl(url);

        JSONObject commitJSONObject = jsonCommit.getJSONObject("commit");

        String message = commitJSONObject.getString("message");
        String name = message.split(": ")[0];

        ZonedDateTime dateTime = ZonedDateTime.parse(commitJSONObject.getJSONObject("committer").getString("date"));

        System.out.println(jsonCommit.getString("sha") + " " + name + " " + dateTime);

        return new Commit(name, message, jsonCommit.getString("sha"), dateTime);
    }

    public static List<Release> getReleases(String projOwner, String projName) throws IOException {
        List<Release> releases = new ArrayList<>();
        int page = 1;
        JSONArray jsonReleases;

        do {
            String url = "https://api.github.com/repos/" + projOwner + "/" + projName + "/tags?per_page=100&page=" + page;
            jsonReleases = readJsonArrayFromUrl(url);

            for(int i = 0; i < jsonReleases.length(); i++) {
                JSONObject obj = (JSONObject) jsonReleases.get(i);
                String commitUrl = obj.getJSONObject("commit").getString("url");


                Release release = new Release(obj.getJSONObject("commit").getString("url"), obj.getString("name"));
                release.setCommit(getCommitFromUrl(commitUrl));
                releases.add(release);
            }

            page++;

        } while(jsonReleases.length() > 0);

        for(int i = 0; i < releases.size(); i++) {
            releases.get(i).setNumber(releases.size() - i);
        }

        return releases;
    }

    public static List<Commit> getCommits(String projOwner, String projName) throws IOException {
        List<Commit> commits = new ArrayList<>();
        int page = 1;
        JSONArray jsonReleases;

        do {
            String url = "https://api.github.com/repos/" + projOwner + "/" + projName + "/commits?per_page=100&page=" + page;
            jsonReleases = readJsonArrayFromUrl(url);

            for(int i = 0; i < jsonReleases.length(); i++) {
                JSONObject obj = (JSONObject) jsonReleases.get(i);

                Commit commit;
                //commits.add(commit);
            }

            System.out.println("Doing page " + page + " with " + jsonReleases.length() + " releases");
            page++;

        } while(jsonReleases.length() > 0);

        return commits;
    }

    public static void main(String[] argv) throws GitAPIException, IOException {

        String projName ="avro";
        String projOwner = "apache";

        int i = 0;
        int j;

        int total;

        List<Release> releases = getReleases(projOwner, projName);
        System.out.println(releases.size());
        //If the directory exists remove it

        /*
        final File localPath = new File("./TestRepo");

        if(!localPath.exists()) {
            //Clone repo from GitHub
            Git.cloneRepository()
                    .setURI(AVRO_URL)
                    .setDirectory(localPath)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("***", "***"))
                    .call();
        }



        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(new File("./TestRepo/.git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build();

        System.out.println(Git.lsRemoteRepository().setRemote(AVRO_URL).setTags(true).call());

        Git git = new Git(repository);

        LogCommand log = git.log();
        Iterable<RevCommit> logs = log.call();
        for (RevCommit rev : logs) {
            System.out.println("Commit: " + rev + " Name: " + rev.getName() + " ----- ");

        }

        List<Ref> tagList = git.tagList().call();
        RevWalk walk = new RevWalk(repository);

        for (Ref ref : tagList) {

            System.out.println("Tag: " + ref.getName());
        }*/

    }
}
