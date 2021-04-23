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
    private static final String PROJECTURL = "https://github.com/apache/";

    public static void main(String[] argv) throws IOException, GitAPIException {

        // git show 357b8fad6464ab25f8e03385ca54d1ce8ec63543 --shortstat --format="" to get stats
        // git log --  src/java/org/apache/avro/Protocol.java to get file change history
        // git show --shortstat --format="" 338db27462cbb442a60033f99fde7d92f863b28a -- lang/c++/test/DataFileTests.cc
        // git log --pretty=format:"{\"hash\":%H, \"commit_date\":%cd, \"author\":%an, \"message\":%s}"

        String projName = "bookkeeper";
        String projOwner = "apache";

        final File localPath = new File("./TestRepo");

        GitHubBoundary gitHubBoundary = new GitHubBoundary(projOwner, projName, 0.5);
        JiraBoundary jiraBoundary = new JiraBoundary();

        if (!localPath.exists()) {
            LOGGER.log(Level.INFO, "Repository not found, downloading...");
            //Clone repo from GitHub
            Git.cloneRepository()
                    .setURI(PROJECTURL + projName + ".git")
                    .setDirectory(localPath)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("***", "***"))
                    .call();
            LOGGER.log(Level.INFO, "Download complete");
        }

        LOGGER.log(Level.INFO, "Fetching releases...");
        List<Release> allReleases = jiraBoundary.getReleases(projName, localPath);
        List<Release> releases = jiraBoundary.getFirstPercentOfReleases(allReleases, 0.5);
        LOGGER.log(Level.INFO, "Number of releases: {0}", releases.size());

        LOGGER.log(Level.INFO, "Fetching commits...");
        List<Commit> commitList = gitHubBoundary.getCommits(localPath);
        LOGGER.log(Level.INFO, "Number of commits: {0}", commitList.size());

        LOGGER.log(Level.INFO, "Fetching issues...");
        List<Issue> issues = jiraBoundary.getBugs(projName, allReleases);
        LOGGER.log(Level.INFO, "Number of issues: {0}", issues.size());

        gitHubBoundary.assignFilesToReleases(releases, commitList, localPath);

        LOGGER.log(Level.INFO, "Setting issues files");
        gitHubBoundary.setIssueAffectFile(issues, localPath);

        //Assign bugs
        for (Issue issue : issues) {
            Release injectVersion = issue.getInjectVersion();
            Release fixVersion = issue.getFixVersion();

            boolean setBugs = false;

            for (Release rel : releases) {
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
