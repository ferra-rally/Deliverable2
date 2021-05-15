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

    public Map<String, Double> compareClassifiers(Instances training, Instances testing, Classifier classifier) throws Exception {
        Map<String, Double> map = new HashMap<>();

        int classIndex = 0;

        classifier.buildClassifier(training);

        Evaluation eval = new Evaluation(testing);

        eval.evaluateModel(classifier, testing);

        double auc = eval.areaUnderROC(classIndex);
        double kappa = eval.kappa();
        double precision = eval.precision(classIndex);
        double recall = eval.recall(classIndex);

        double tp = eval.numTruePositives(classIndex);
        double tn = eval.numTrueNegatives(classIndex);
        double fp = eval.numFalsePositives(classIndex);
        double fn = eval.numFalseNegatives(classIndex);

        map.put("auc", auc);
        map.put("kappa", kappa);
        map.put("precision", precision);
        map.put("recall", recall);

        map.put("tp", tp);
        map.put("fp", fp);
        map.put("tn", tn);
        map.put("fn", fn);

        return map;
    }

    private String walkForward(String projName, String projOwner, List<Classifier> classifiers) {
        StringBuilder builder = new StringBuilder();
        String format = "%s,%d,%.0f%%,%s,%.0f,%.0f,%.0f,%.0f,%s,%f,%f,%f\n";

        try {
            ConverterUtils.DataSource source = new ConverterUtils.DataSource("./out/" + projName + "_" + projOwner + "_out.arff");
            Instances instances = source.getDataSet();
            //dataset, #TrainingRelease, %training (data on training / total data), %Defective in training, %Defective in testing, EPVbeforeFeatureSelection, EPVafterFeatureSelection,classifier, balancing, Feature Selection,TP,  FP,  TN, FN, Precision, Recall, ROC Area, Kappa

            int numReleases = instances.attribute(0).numValues();
            List<Instances> instancesList = splitDataSet(instances, numReleases);

            for(int i = 0; i < numReleases - 1; i++) {
                Instances testing = instancesList.get(i + 1);

                Instances training = new Instances(instancesList.get(0), 0);
                for(int j = 0; j <= i; j++) {
                    training.addAll(instancesList.get(j));
                }

                int trainingSize = training.size();
                int testingSize = testing.size();

                double percentTraining = (trainingSize / (trainingSize + testingSize * 1.0)) * 100;

                for(Classifier classifier : classifiers) {
                    Map<String, Double> map = compareClassifiers(training, testing, classifier);

                    double precision = map.get("precision");
                    String precisionString;
                    if(Double.isNaN(precision)) {
                        precisionString = "?";
                    } else {
                        precisionString = precision + "";
                    }
                    String longName = classifier.getClass().getName();
                    String[] tokens = longName.split("\\.");

                    String name = tokens[tokens.length - 1];

                    String row = String.format(Locale.US, format,
                            projName, i + 1, percentTraining, name,
                            map.get("tp"), map.get("fp"), map.get("tn"), map.get("fn"),
                            precisionString, map.get("recall"), map.get("auc"), map.get("kappa"));

                    builder.append(row);
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error when evaluating");
        }

        return builder.toString();
    }

    public String walkForward(List<String> projectList, String projOwner, List<Classifier> classifiers) {
        StringBuilder builder = new StringBuilder();
        builder.append("Dataset,#TrainingRelease,%training,Classifier,TP,FP,TN,FN,Precision,Recall,AUC,Kappa\n");

        for(String projName : projectList) {
            builder.append(walkForward(projName, projOwner, classifiers));
        }

        return builder.toString();
    }
}
