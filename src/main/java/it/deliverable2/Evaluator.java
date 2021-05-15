package it.deliverable2;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;

import java.util.*;
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

    private double countPositiveInstances(Instances instances) {
        double count = 0;

        for(Instance instance : instances) {
            count += (int) instance.value(instance.numAttributes() - 1) == 1 ? 1 : 0;
        }

        return count;
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

    private List<Object> applyFilter(Instances instances, List<Filter> filters, int filterNumber) throws Exception {
        List<Object> list = new ArrayList<>();
        Filter filter;

        //Apply filters, if filter number is equal to -1
        if(filterNumber == -1) {
            list.add("no_sampling");
            list.add(instances);

            return list;
        } else {
            filter = filters.get(filterNumber);
        }

        if(filter.getClass().getName().contains("SMOTE")) {
                //SMOTE
            list.add("SMOTE");
            //TODO IMPLEMENT SMOTE
            list.add(instances);
        } else if(filters.get(filterNumber).getClass().getName().contains("SpreadSubsample")) {
            //Undersampling
            SpreadSubsample undersampling = (SpreadSubsample) filter;
            undersampling.setDistributionSpread(1.0);
            undersampling.setInputFormat(instances);

            list.add("undersampling");
            list.add(Filter.useFilter(instances, filter));
        } else if(filters.get(filterNumber).getClass().getName().contains("Resample")) {
            //Oversampling
            Resample oversample = (Resample) filter;
            oversample.setInputFormat(instances);
            oversample.setBiasToUniformClass(1.0);
            oversample.setNoReplacement(false);

            int positiveClasses = (int) countPositiveInstances(instances);
            int size = instances.size();

            int majority;
            int minority;

            if(positiveClasses > (size / 2)) {
                majority = positiveClasses;
                minority = size - positiveClasses;
            } else {
                minority = positiveClasses;
                majority = size - positiveClasses;
            }

            double y = 0;
            if(minority != 0) {
                y = 100 * (1.0 * majority - minority) / minority;
            }

            oversample.setSampleSizePercent(y);

            list.add("oversample");
            list.add(Filter.useFilter(instances, filter));
        }

        return list;
    }


    private String walkForward(String projName, String projOwner, List<Classifier> classifiers, List<Filter> filters) throws Exception {
        StringBuilder builder = new StringBuilder();
        String format = "%s,%d,%.0f%%,%.0f%%,%.0f%%,%s,%s,%.0f,%.0f,%.0f,%.0f,%s,%f,%f,%f\n";

            ConverterUtils.DataSource source = new ConverterUtils.DataSource("./out/" + projName + "_" + projOwner + "_out.arff");
            Instances instances = source.getDataSet();
            //dataset, #TrainingRelease, %training (data on training / total data), %Defective in training, %Defective in testing, EPVbeforeFeatureSelection, EPVafterFeatureSelection,classifier, balancing, Feature Selection,TP,  FP,  TN, FN, Precision, Recall, ROC Area, Kappa

            int numReleases = instances.attribute(0).numValues();
            List<Instances> instancesList = splitDataSet(instances, numReleases);

            //Walk forward
            for(int i = 0; i < numReleases - 1; i++) {
                Instances testing = new Instances(instancesList.get(0), 0);
                testing.addAll(instancesList.get(i + 1));

                Instances training = new Instances(instancesList.get(0), 0);
                for (int j = 0; j <= i; j++) {
                    training.addAll(instancesList.get(j));
                }

                //Test different filters
                for (int filterNumber = -1; filterNumber < filters.size(); filterNumber++) {
                    Instances sampledTraining;

                    List<Object> filterOut = applyFilter(training, filters, filterNumber);
                    String filterName = (String) filterOut.get(0);
                    sampledTraining = (Instances) filterOut.get(1);

                    int trainingSize = sampledTraining.size();
                    int testingSize = testing.size();

                    double percentTraining = (trainingSize / (trainingSize + testingSize * 1.0)) * 100;

                    double trainingPositiveInstances = countPositiveInstances(sampledTraining);
                    double testingPositiveInstances = countPositiveInstances(testing);

                    double defectiveTrainingPercent = (trainingPositiveInstances / trainingSize) * 100;
                    double defectiveTestingPercent = (testingPositiveInstances / testingSize) * 100;

                    for (Classifier classifier : classifiers) {
                        Map<String, Double> map = compareClassifiers(sampledTraining, testing, classifier);

                        double precision = map.get("precision");
                        String precisionString;
                        if (Double.isNaN(precision)) {
                            precisionString = "?";
                        } else {
                            precisionString = precision + "";
                        }
                        String longName = classifier.getClass().getName();
                        String[] tokens = longName.split("\\.");

                        String name = tokens[tokens.length - 1];

                        String row = String.format(Locale.US, format,
                                projName, i + 1, percentTraining, defectiveTrainingPercent, defectiveTestingPercent, filterName,
                                name,
                                map.get("tp"), map.get("fp"), map.get("tn"), map.get("fn"),
                                precisionString, map.get("recall"), map.get("auc"), map.get("kappa"));

                        builder.append(row);
                    }
                }
            }


        return builder.toString();
    }

    public String walkForward(List<String> projectList, String projOwner, List<Classifier> classifiers, List<Filter> filters) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("Dataset,#TrainingRelease,%training,%Defective_in_training,%Defective_in_testing,Filter,Classifier,TP,FP,TN,FN,Precision,Recall,AUC,Kappa\n");

        for(String projName : projectList) {
            builder.append(walkForward(projName, projOwner, classifiers, filters));
        }

        return builder.toString();
    }
}
