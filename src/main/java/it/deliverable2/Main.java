package it.deliverable2;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String PROJECTURL = "https://github.com/apache/avro.git";

    public static void main(String[] argv) throws IOException, GitAPIException {

        String projName = "avro";
        String projOwner = "apache";

        final File localPath = new File("./TestRepo");

        GitHubBoundary gitHubBoundary = new GitHubBoundary(projOwner, projName, 0.5);
        JiraBoundary jiraBoundary = new JiraBoundary();

        LOGGER.log(Level.INFO, "Fetching releases...");
        List<Release> allReleases = jiraBoundary.getReleases(projName, localPath);
        List<Release> releases = jiraBoundary.getFirstPercentOfReleases(allReleases, 0.5);
        LOGGER.log(Level.INFO, "Number of releases: {0}", releases.size());

        /*
        LOGGER.log(Level.INFO, "Fetching commits...");
        List<Commit> commits = gitHubBoundary.getCommits(projOwner, projName);
        List<Commit> commitsForReleases = new ArrayList<>(commits);
        LOGGER.log(Level.INFO, "Number of commits: {0}", commits.size());

        LOGGER.log(Level.INFO, "Assigning commits to releases...");
        for(Release rel : releases) {
            LOGGER.log(Level.INFO, "Doing release {0}", rel.getName());
            ZonedDateTime date = rel.getDate();

            List<Commit> commitsOfRelease = new ArrayList<>();

            //Assign commits to releases
            for(Iterator<Commit> iterator = commitsForReleases.iterator(); iterator.hasNext(); ) {
                Commit commit = iterator.next();
                if(commit.getDate().isBefore(date)) {
                    commitsOfRelease.add(commit);
                    iterator.remove();
                }
            }

            rel.setCommits(commitsOfRelease);
            LOGGER.log(Level.INFO, "Release {0} with {1} of commits", new Object[]{rel.getName(), commitsOfRelease.size()});
        }

        //Get commits details
        for(Release rel : releases) {
            List<Commit> commitsOfRelease = rel.getCommits();

            for(Commit commit : commitsOfRelease) {
                JSONObject jsonObject = gitHubBoundary.readJsonFromUrlGitHub(commit.getCommitUrl());

                commit.setJsonObject(jsonObject);
            }
        }
        */

        if (!localPath.exists()) {
            LOGGER.log(Level.INFO, "Repository not found, downloading...");
            //Clone repo from GitHub
            Git.cloneRepository()
                    .setURI(PROJECTURL)
                    .setDirectory(localPath)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("***", "***"))
                    .call();
            LOGGER.log(Level.INFO, "Download complete");
        }

        gitHubBoundary.setReleaseFiles(releases, localPath);

        List<Issue> issues = jiraBoundary.getBugs("avro", allReleases);

        LOGGER.log(Level.INFO, "Setting issues files");
        gitHubBoundary.setIssueAffectFile(issues, localPath);

        //Assign bugs
        for (Issue issue : issues) {
            Release injectVersion = issue.getInjectVersion();
            Release fixVersion = issue.getFixVersion();

            boolean setBugs = false;

            for (int i = 0; i < releases.size(); i++) {
                Release rel = releases.get(i);

                if (rel.equals(injectVersion)) {
                    setBugs = true;
                } else if (rel.equals(fixVersion)) {
                    break;
                }

                if (setBugs && issue.getAffects() != null) {
                    rel.setBugs(issue.getAffects());
                }
            }
        }

        //Write CSV
        Utils.writeCsv(projName, projOwner, releases);
    }
}
