package com.sharedtable;

public class Constants {

    public static String getNoPasswordConstant() {
        return "NO_PASSWORD";
    }

    public static int getBuildNumber() {
        return 21;
    }

    public static boolean isPlatformWindows() {
        return isWindows;
    }

    public static boolean isPlatformLinux() {
        return isLinux;
    }


    public static String getPLatformString() {
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

    private static boolean isWindows = System.getProperty("os.name").contains("Windows");
    private static boolean isLinux = System.getProperty("os.name").contains("Linux");
}