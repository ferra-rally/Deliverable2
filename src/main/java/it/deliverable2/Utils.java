package it.deliverable2;

public class Utils {
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
}
