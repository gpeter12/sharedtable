package com.sharedtable;

public class Constants {

    public static String getNoPasswordConstant() {
        return "NO_PASSWORD";
    }

    public static int getBuildNumber() {
        return 25;
    }

    public static boolean isPlatformWindows() {
        return isWindows;
    }

    public static boolean isPlatformLinux() {
        return isLinux;
    }


    public static String getPlatformString() {
        if (Constants.isPlatformLinux()) {
            return "Linux";
        } else if (Constants.isPlatformWindows()) {
            return "Windows";
        }
        return "Unknown";
    }

    public static String getNetworkPasswordValidationOK() {
        return "PASSWD_OK";
    }

    public static String getNetworkPasswordValidationINVALID() {
        return "PASSWD_INVALID";
    }

    public static void setIsPortableRun(boolean val) {
        isPortableRun = val;
    }

    public static boolean isPortableRun() {return isPortableRun;}

    private static boolean isPortableRun = false;
    private static boolean isWindows = System.getProperty("os.name").contains("Windows");
    private static boolean isLinux = System.getProperty("os.name").contains("Linux");

}