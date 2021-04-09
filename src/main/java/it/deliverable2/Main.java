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

    public static void main(String[] argv) throws GitAPIException, IOException {

        String projName ="avro";
        String projOwner = "apache";

        GitHubBoundary gitHubBoundary = new GitHubBoundary();

        //List<Release> releases = gitHubBoundary.getReleases(projOwner, projName);
        //System.out.println(releases.size());
        List<Commit> commits = gitHubBoundary.getCommits(projOwner, projName);

        System.out.println(commits.size());
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
