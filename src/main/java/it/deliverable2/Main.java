package it.deliverable2;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
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
        final File localPath = new File("./TestRepo");

        //If the directory exists remove it

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

        /*
        Iterable<RevCommit> log = git.log().all().call();

        System.out.println("Commits:");

        for (RevCommit rev : log) {
            System.out.println(rev.getShortMessage());

        }


        Process process  = Runtime.getRuntime().exec("git log");
        System.out.println(process.getOutputStream();

        */




        LogCommand log = git.log();
        Iterable<RevCommit> logs = log.call();
        for (RevCommit rev : logs) {
            System.out.println("Commit: " + rev + " Name: " + rev.getName() + " ----- ");

        }

        List<Ref> tagList = git.tagList().call();
        RevWalk walk = new RevWalk(repository);

        for (Ref ref : tagList) {

            System.out.println("Tag: " + ref.getName());
        }

    }
}
