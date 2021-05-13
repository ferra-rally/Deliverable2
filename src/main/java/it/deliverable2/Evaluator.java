package it.deliverable2;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Evaluator {
    private static final Logger LOGGER = Logger.getLogger(Evaluator.class.getName());

    public List<Instances> splitDataSet(Instances instances, int numReleases) {
        List<Instances> setList = new ArrayList<>();
        int index = 0;

        List<String> releases = new ArrayList<>();

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

    public Map<String, String> compareClassifiers(Instances training, Instances testing, Classifier classifier) throws Exception {
        Map<String, String> map = new HashMap<>();

        classifier.buildClassifier(training);

        Evaluation eval = new Evaluation(testing);

        eval.evaluateModel(classifier, testing);

        double auc = eval.areaUnderROC(0);
        double kappa = eval.kappa();
        double precision = eval.precision(0);
        double recall = eval.recall(0);

        String precisionString;
        if(Double.isNaN(precision)) {
            precisionString = "?";
        } else {
            precisionString = precision + "";
        }

        map.put("auc", auc + "");
        map.put("kappa", kappa + "");
        map.put("precision", precisionString);
        map.put("recall", recall + "");

        return map;
    }

    public String walkForward(String projName, String projOwner, List<Classifier> classifiers) {
        StringBuilder builder = new StringBuilder();
        String format = "%s,%d,%s,%s,%s,%s,%s\n";

        try {
            ConverterUtils.DataSource source = new ConverterUtils.DataSource("./out/" + projName + "_" + projOwner + "_out.arff");
            Instances instances = source.getDataSet();

            builder.append("Dataset,#TrainingRelease,Classifier,Precision,Recall,AUC,Kappa\n");

            int numReleases = instances.attribute(0).numValues();
            List<Instances> instancesList = splitDataSet(instances, numReleases);

            for(int i = 0; i < numReleases - 1; i++) {
                Instances testing = instancesList.get(i + 1);

                Instances training = new Instances(instancesList.get(0), 0);
                for(int j = 0; j <= i; j++) {
                    training.addAll(instancesList.get(j));
                }

                for(Classifier classifier : classifiers) {
                    Map<String, String> map = compareClassifiers(training, testing, classifier);

                    String longName = classifier.getClass().getName();
                    String[] tokens = longName.split("\\.");

                    String name = tokens[tokens.length - 1];

                    builder.append(String.format(Locale.US, format, projName, i + 1, name, map.get("precision"), map.get("recall"),
                            map.get("auc"), map.get("kappa")));
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load data source from arff");
        }

        return builder.toString();
    }
}
