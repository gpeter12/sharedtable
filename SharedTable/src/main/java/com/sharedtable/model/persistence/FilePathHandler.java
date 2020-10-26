package com.sharedtable.model.persistence;

import java.io.File;

public class FilePathHandler {
    private String customConfigPath = null;
    private static FilePathHandler instance = new FilePathHandler();

    private FilePathHandler() {}

    public static FilePathHandler getInstance() {
        if(instance == null) {
            instance = new FilePathHandler();
        }
        return instance;
    }
    public void setCustomConfigPath(String path){
        customConfigPath = path;
    }


    public String getFilePathOnLinux() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDirectoryPathOnLinux())
                .append("/userconfig.json");
        return sb.toString();
    }

    public String getFilePathOnWindows() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDirectoryPathOnWindows())
                .append("\\userconfig.json");
        return sb.toString();
    }

    public String getDirectoryPathOnLinux() {
        if(customConfigPath != null){
            return customConfigPath;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(System.getProperty("user.home"))
                    .append("/.config/SharedTable");
            return sb.toString();
        }
    }

    public String getDirectoryPathOnWindows() {
        if(customConfigPath != null){
            return customConfigPath;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(System.getProperty("user.home"))
                    .append("\\AppData\\Local\\SharedTable");
            return sb.toString();
        }

    }

    public void createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()){
            directory.mkdirs();
        }
    }



}
