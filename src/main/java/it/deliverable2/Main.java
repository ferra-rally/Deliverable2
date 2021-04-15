package it.deliverable2;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger( Main.class.getName() );
    private static final String PROJECTURL = "https://github.com/apache/avro.git";

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

    public static void main(String[] argv) throws IOException, GitAPIException {

        String projName ="avro";
        String projOwner = "apache";

        GitHubBoundary gitHubBoundary = new GitHubBoundary(projOwner, projName, 0.5);

        LOGGER.log(Level.INFO, "Fetching releases...");
        List<Release> releases = gitHubBoundary.getReleases();
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


        final File localPath = new File("./TestRepo");

        if(!localPath.exists()) {
            LOGGER.log(Level.INFO, "Repository not found, downloading...");
            //Clone repo from GitHub
            Git.cloneRepository()
                    .setURI(PROJECTURL)
                    .setDirectory(localPath)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("***", "***"))
                    .call();
            LOGGER.log(Level.INFO, "Download complete");
        }

        Runtime runtime = Runtime.getRuntime();
        //To store name in order
        List<String> releasesNames = new ArrayList<>();

        //Get file of release
        for(Release rel : releases) {
            Process process = runtime.exec("git ls-tree -r " + rel.getFullName() +  " --name-only", null, localPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<RepoFile> releaseFileList = new ArrayList<>();

            //Track only .java files
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.contains(".java")) {
                    releaseFileList.add(new RepoFile(line));
                }
            }

            rel.setFileList(releaseFileList);
            releasesNames.add(rel.getName());
        }

        JiraBoundary jiraBoundary = new JiraBoundary();

        List<Issue> issues = jiraBoundary.getBugs("avro");

        for(Issue issue : issues) {
            Process process = runtime.exec("git log --all --grep=\"" + issue.getName() + "[: .]\"", null, localPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if(line.contains("commit")) {
                    String sha = line.split(" ")[1];
                    if(!sha.isEmpty()) {
                        Commit commit = gitHubBoundary.getCommitFromSha(sha);
                        List<RepoFile> repoFileList = commit.getRepoFileList();

                        issue.setAffects(repoFileList);
                        LOGGER.log(Level.INFO, "Issue {0} with injected {1} and fixed {2} affects {3} files", new Object[]{issue.getName(), issue.getInjectVersion(), issue.getFixVersion(), repoFileList.size()});
                    }
                }
            }
        }

        //Write output in csv
        try (FileWriter outWriter = new FileWriter(projName + "_" + projOwner + "_out.csv")) {
            for(Release rel : releases) {
                int number = rel.getNumber();
                List<RepoFile> fileList = rel.getFileList();

                for (RepoFile file : fileList) {
                    outWriter.write(number + "," + file.getFilename() + "\n");
                }
            }
        }

        /*
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

        }*/
    }
}
