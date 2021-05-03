package it.deliverable2;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.Classifier;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String PROJECTURL = "https://github.com/apache/";
    private static final String PATH_FORMAT = "./out/%s_%s_out.arff";

    public static List<Release> getFirstPercentOfReleases(List<Release> releases, double firstPercentReleases) {
        List<Release> croppedReleasesList = new ArrayList<>();

        int number = (int) Math.floor(releases.size() * (1 - firstPercentReleases));

        for (int i = 0; i < number; i++) {
            croppedReleasesList.add(releases.get(i));
        }

        List<String> releaseNames = new ArrayList<>();
        for (Release rel : croppedReleasesList) {
            releaseNames.add(rel.getName());
        }
        LOGGER.log(Level.INFO, "Used releases {0}", releaseNames);

        return croppedReleasesList;
    }

    private static HashMap<String, List<Commit>> convertListToHashMap(List<Commit> commitList) {
        HashMap<String, List<Commit>> commitMap = new HashMap<>();

        for (Commit commit : commitList) {
            String key = commit.getName();
            if (commitMap.containsKey(key)) {
                commitMap.get(key).add(commit);
            } else {
                List<Commit> commits = new ArrayList<>();
                commits.add(commit);

                commitMap.put(key, commits);
            }
        }

        return commitMap;
    }

    //Generate data and create files
    public static void generateData(String projName, String projOwner) throws IOException, GitAPIException {
        Path path = Paths.get(String.format(PATH_FORMAT, projName, projOwner));

        //Out file already exists
        if (Files.exists(path)) {
            LOGGER.log(Level.INFO, "Found {0} file, returning...", String.format(PATH_FORMAT, projName, projOwner));
            return;
        } else {
            LOGGER.log(Level.INFO, "File {0} not found, generating data...", String.format(PATH_FORMAT, projName, projOwner));
        }

        final File localPath = new File("./repos/" + projName);

        GitHubBoundary gitHubBoundary = new GitHubBoundary(projOwner, projName, 0.5);
        JiraBoundary jiraBoundary = new JiraBoundary();

        //Download repositor
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
        List<Release> releases = getFirstPercentOfReleases(allReleases, 0.5);
        LOGGER.log(Level.INFO, "Number of releases: {0}", releases.size());

        LOGGER.log(Level.INFO, "Fetching commits...");
        List<Commit> commitList = gitHubBoundary.getCommits(localPath);
        HashMap<String, List<Commit>> commitMap = convertListToHashMap(commitList);
        LOGGER.log(Level.INFO, "Number of commits: {0}", commitList.size());

        LOGGER.log(Level.INFO, "Fetching issues...");
        List<Issue> issues = jiraBoundary.getBugs(projName, allReleases, commitMap);
        LOGGER.log(Level.INFO, "Number of issues: {0}", issues.size());

        gitHubBoundary.assignFilesToReleases(releases, commitList, localPath);

        LOGGER.log(Level.INFO, "Setting issues files");
        gitHubBoundary.setIssueAffectFile(issues, commitMap);

        //Assign bugs
        for (Issue issue : issues) {
            Release injectVersion = issue.getInjectVersion();
            Release fixVersion = issue.getFixVersion();

            boolean setBugs = false;

            for (Release rel : releases) {
                if (rel.equals(injectVersion) && !injectVersion.equals(fixVersion)) {
                    setBugs = true;
                } else if (rel.equals(fixVersion)) {
                    rel.setFix(issue);
                    break;
                }

                if (setBugs && issue.getAffects() != null) {
                    rel.setBugs(issue);
                }
            }
        }

        //Write CSV
        Utils.writeCsv(projName, projOwner, releases);
    }

    public static List<Instances> splitDataSet(Instances instances) {
        List<Instances> setList = new ArrayList<>();
        int index = 0;

        List<String> releases = new ArrayList<>();
        int numReleases = instances.attribute(index).numValues();

        for (int i = 0; i < numReleases; i++) {
            releases.add(instances.attribute(0).value(i));
        }

        //Separate instances based on the release
        for (String releaseNumber : releases) {
            Instances filteredInstances = new Instances(instances, 0); // Empty Instances with same header
            instances.parallelStream()
                    .filter(instance -> instance.stringValue(index).equals(releaseNumber))
                    .forEachOrdered(filteredInstances::add);

            setList.add(filteredInstances);

            filteredInstances.setClassIndex(filteredInstances.numAttributes() - 1);
        }

        return setList;
    }

    public static Map<String, Double> compareClassifiers(Instances training, Instances testing, Classifier classifier) throws Exception {
        Map<String, Double> map = new HashMap<>();


        classifier.buildClassifier(training);

        Evaluation eval = new Evaluation(testing);

        eval.evaluateModel(classifier, testing);

        double auc = eval.areaUnderROC(1);
        double kappa = eval.kappa();
        double precision = eval.precision(1);
        double recall = eval.recall(1);

        map.put("auc", auc);
        map.put("kappa", kappa);
        map.put("precision", precision);
        map.put("recall", recall);

        return map;
    }

    public static String walkForward() {
        return "";
    }

    public static void main(String[] argv) throws IOException, GitAPIException {

        // git show 357b8fad6464ab25f8e03385ca54d1ce8ec63543 --shortstat --format="" to get stats
        // git log --  src/java/org/apache/avro/Protocol.java to get file change history
        // git show --shortstat --format="" 338db27462cbb442a60033f99fde7d92f863b28a -- lang/c++/test/DataFileTests.cc
        // git log --pretty=format:"{\"hash\":%H, \"commit_date\":%cd, \"author\":%an, \"message\":%s}"

        String projName = "avro";
        String projOwner = "apache";

        generateData(projName, projOwner);

        try {
            DataSource source = new DataSource("./out/" + projName + "_" + projOwner + "_out.arff");
            Instances instances = source.getDataSet();

            List<Instances> instancesList = splitDataSet(instances);
            List<Classifier> classifiers = new ArrayList<>();
            classifiers.add(new NaiveBayes());
            classifiers.add(new RandomForest());

            System.out.println(compareClassifiers(instancesList.get(0), instancesList.get(1), classifiers.get(0)));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load data source from arff");
        }
    }
}
