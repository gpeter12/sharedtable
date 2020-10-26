package com.sharedtable;

import com.sharedtable.model.persistence.FilePathHandler;

import java.util.logging.FileHandler;

public class LoggerConfig {


    private static FileHandler fh=null;

    static  {
        try {
            if (Constants.isPlatformWindows()) {
                FilePathHandler.getInstance().createDirectory(FilePathHandler.getInstance().getDirectoryPathOnWindows());
                fh = new FileHandler(FilePathHandler.getInstance().getDirectoryPathOnWindows() + "\\logfile.log");
            } else if (Constants.isPlatformLinux()) {
                FilePathHandler.getInstance().createDirectory(FilePathHandler.getInstance().getDirectoryPathOnLinux());
                fh = new FileHandler(FilePathHandler.getInstance().getDirectoryPathOnLinux() + "/logfile.log");
            }
        } catch (Exception e) {
            System.out.println("creating logfile error");
        }
    }
}
