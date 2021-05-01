package it.deliverable2;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {
    private static final Logger LOGGER = Logger.getLogger( Utils.class.getName() );
    private static final String DIRECTORY = "./out/";

    private Utils() {

    }

    public static boolean compareVersionString(String version1, String version2) {
        String[] tokens1 = version1.split(".");
        String[] tokens2 = version2.split(".");

        int min;
        min = Math.min(tokens1.length, tokens2.length);

        for(int i = 0; i < min; i++) {
            int intToken1 =  Integer.parseInt(tokens1[i]);
            int intToken2 =  Integer.parseInt(tokens2[i]);
            if(intToken1 > intToken2) {
                return true;
            } else if(intToken1 < intToken2) {
                return false;
            }
        }

        return tokens1.length > tokens2.length;
    }

    public static String getReleaseHeader(List<Release> releases) {
        int releaseNum = releases.size();
        StringBuilder builder = new StringBuilder();
        builder.append("{");

        for(int i = 1; i < releaseNum; i++) {
            builder.append(i).append(",");
        }

        builder.append(releaseNum);

        builder.append("}");

        return builder.toString();
    }

    public static void writeCsv(String projName, String projOwner, List<Release> releases) {
        String attributeString = "@ATTRIBUTE ";
        String numericString = " NUMERIC\n";
        //Write output in csv
        try (FileWriter csvWriter = new FileWriter(DIRECTORY + projName + "_" + projOwner + "_out.csv");
             FileWriter arffWriter = new FileWriter(DIRECTORY + projName + "_" + projOwner + "_out.arff")) {

            //Write arf header
            arffWriter.write("@RELATION " + projName + "\n\n");

            arffWriter.write(attributeString + "Release " + getReleaseHeader(releases) + "\n");
            arffWriter.write(attributeString + "LOC" + numericString);
            arffWriter.write(attributeString + "LOC_touched" + numericString);
            arffWriter.write(attributeString + "NR" + numericString);
            arffWriter.write(attributeString + "NFix" + numericString);
            arffWriter.write(attributeString + "NAuth" + numericString);
            arffWriter.write(attributeString + "LOC_added" + numericString);
            arffWriter.write(attributeString + "MAX_LOC_added" + numericString);
            arffWriter.write(attributeString + "AVG_LOC_added" + numericString);
            arffWriter.write(attributeString + "Churn" + numericString);
            arffWriter.write(attributeString + "MAX_Churn" + numericString);
            arffWriter.write(attributeString + "AVG_Churn" + numericString);
            arffWriter.write(attributeString + "MAX_ChgSet" + numericString);
            arffWriter.write(attributeString + "AVG_ChgSet" + numericString);
            arffWriter.write(attributeString + "Age" + numericString);
            arffWriter.write(attributeString + "Weighted_Age" + numericString);

            arffWriter.write("@ATTRIBUTE " + "Buggy" + " {Yes, No}\n\n");

            arffWriter.write("@DATA\n");

            //Write csv header
            csvWriter.write("Release, Filename, LOC, LOC_touched, NR, NFix, NAuth, LOC_added, MAX_LOC_added, AVG_LOC_added, Churn, MAX_Churn, AVG_Churn, MAX_ChgSet, AVG_ChgSet, Age, Weighted_Age, Buggy\n");
            for (Release rel : releases) {
                int number = rel.getNumber();
                ZonedDateTime releaseDate = rel.getDate();

                List<ReleaseFile> fileList = rel.getFileList();

                for (ReleaseFile file : fileList) {
                    //Age in milliseconds
                    ZonedDateTime insertionDate = file.getInsertDate();
                    String ageString;
                    String weighetAgeString;
                    int locTouched = file.getChanges();

                    if(insertionDate != null) {
                        long duration = Math.abs(Duration.between(releaseDate, insertionDate).toMillis());
                        long weightedAge = locTouched * duration;
                        weighetAgeString = weightedAge + "";
                        ageString = duration + "";
                    } else {
                        ageString = "?";
                        weighetAgeString = "?";
                    }

                    String attributesString = file.getLoc() + "," + locTouched + "," + file.getNumOfRevision() + ","
                            + file.getFixes() + "," + file.getNumOfAuthors() + "," + file.getLocAdded() + "," +
                            file.getMaxLocAdded() + "," + file.getAvgLocAdded() + "," + file.getChurn() + "," + file.getMaxChurn() + "," +
                            file.getAvgChurn() + "," + file.getMaxChgSetSize() + "," + file.getAvgChgSetSize() + "," + ageString + "," + weighetAgeString + "," + file.isBuggy() + "\n";

                    csvWriter.write(number + "," + file.getFilename() + "," + attributesString);
                    arffWriter.write(number + "," + attributesString);
                }
            }
            LOGGER.log(Level.SEVERE, "Done generating arff and csv");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to write csv or arff file");
        }
    }
}
