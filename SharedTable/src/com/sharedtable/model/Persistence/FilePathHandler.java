package com.sharedtable.model.Persistence;

import com.sharedtable.Constants;

import java.io.File;

public class FilePathHandler {
    public static String getFilePathOnLinux() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDirectoryPathOnLinux())
                .append("/userconfig.json");
        return sb.toString();
    }

    public static String getFilePathOnWindows() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDirectoryPathOnWindows())
                .append("\\userconfig.json");
        return sb.toString();
    }

    public static String getDirectoryPathOnLinux() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("user.home"))
                .append("/.config/SharedTable");
        return sb.toString();
    }

    public static String getDirectoryPathOnWindows() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("user.home"))
                .append("\\AppData\\Local\\SharedTable");
        return sb.toString();
    }

    public static void createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()){
            directory.mkdirs();
        }
    }

}
