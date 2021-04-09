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
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger( Main.class.getName() );
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

        LOGGER.log(Level.INFO, "Fetching relases...");
        List<Release> releases = gitHubBoundary.getReleases(projOwner, projName);
        LOGGER.log(Level.INFO, "Number of relases: {0}", releases.size());

        LOGGER.log(Level.INFO, "Fetching commits...");
        List<Commit> commits = gitHubBoundary.getCommits(projOwner, projName);
        List<Commit> commitsForReleases = new ArrayList<>(commits);
        LOGGER.log(Level.INFO, "Number of commits: {0}", commits.size());

        LOGGER.log(Level.INFO, "Assigning commits to realses...");
        for(Release rel : releases) {
            LOGGER.log(Level.INFO, "Doing release {0}", rel.getName());
            ZonedDateTime date = rel.getDate();

            List<Commit> commitsOfRelease = new ArrayList<>();

            for(Commit commit : commitsForReleases) {
                if(commit.getDate().isBefore(date)) {
                    commitsOfRelease.add(commit);
                    commitsOfRelease.remove(commit);
                }
            }

            rel.setCommits(commitsOfRelease);
            LOGGER.log(Level.INFO, "Release {0} with {1}", new Object[]{rel.getName(), commitsOfRelease.size()});
        }
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
