package it.deliverable2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {
    private static final Logger LOGGER = Logger.getLogger( Utils.class.getName() );

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

    public static void writeCsv(String projName, String projOwner, List<Release> releases) {
        //Write output in csv
        try (FileWriter outWriter = new FileWriter(projName + "_" + projOwner + "_out.csv")) {
            outWriter.write("Release, Filename, LOC, LOC_touched, NR, NFix, NAuth, LOC_added, MAX_LOC_added, AVG_LOC_added, Churn, MAX_Churn, MAX_ChgSet, AVG_ChgSet, Age, Buggy\n");
            for (Release rel : releases) {
                int number = rel.getNumber();
                ZonedDateTime releaseDate = rel.getDate();

                List<ReleaseFile> fileList = rel.getFileList();

                for (ReleaseFile file : fileList) {
                    //Age in milliseconds
                    ZonedDateTime insertionDate = file.getInsertDate();
                    String ageString;
                    int locTouched = file.getChanges();

                    if(insertionDate != null) {
                        ageString = Duration.between(releaseDate, insertionDate).toMillis() + "";
                    } else {
                        ageString = "?";
                    }

                    outWriter.write(number + "," + file.getFilename() + "," + file.getLoc() + "," + locTouched + "," + file.getNumOfRevision()
                            + file.getFixes() + "," + "," + file.getNumOfAuthors() + "," + file.getLocAdded() + "," +
                            file.getMaxLocAdded() + "," + file.getAvgLoxAdded() + "," + file.getChurn() + "," + file.getMaxChurn() + "," +
                            file.getAvgChurn() + "," + file.getMaxChgSetSize() + "," + file.getAvgChgSetSize() + "," + ageString + "," + file.isBuggy() + "\n");
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to write csv file");
        }
    }

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
}
