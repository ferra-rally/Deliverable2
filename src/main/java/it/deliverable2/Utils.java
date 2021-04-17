package it.deliverable2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
            outWriter.write("Release, Filename, LOC, Buggy\n");
            for (Release rel : releases) {
                int number = rel.getNumber();
                List<RepoFile> fileList = rel.getFileList();

                for (RepoFile file : fileList) {
                    outWriter.write(number + "," + file.getFilename() + "," + file.getLoc() + "," + file.isBuggy() + "\n");
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
