package it.deliverable2;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
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
        /*
        if(localPath.exists()) {
            System.out.println("Directory exists");
            deleteDirectory(localPath);
        }

        //Clone repo from GitHub
        Git.cloneRepository()
                .setURI(AVRO_URL)
                .setDirectory(localPath)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("***", "***"))
                .call();
        */
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(new File("./TestRepo/.git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build();

        System.out.println(Git.lsRemoteRepository().setRemote(AVRO_URL).setTags(true).call());

        Git git = new Git(repository);

        Iterable<RevCommit> log = git.log().all().call();

        System.out.println("Commits:");

        for (Iterator<RevCommit> iterator = log.iterator(); iterator.hasNext();) {
            RevCommit rev = iterator.next();

            System.out.println(rev.getShortMessage());

        }

        List<Ref> call = git.tagList().call();
        for (Ref ref : call) {

            System.out.println("Tag: " + ref.getName());


        }



    }
}
