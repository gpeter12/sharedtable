package com.sharedtable;

import com.sharedtable.model.persistence.FilePathHandler;

import java.util.logging.FileHandler;

public class LoggerConfig {

    /*public static Logger setLogger(Logger logger){

        logger.addHandler(fh);
        logger.setLevel(Level.FINEST);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        logger.getHandlers()[0].setLevel(Level.FINEST);

        return logger;
    }*/

    private static FileHandler fh=null;

    static  {
        try {

            if (Constants.isPlatformWindows()) {
                FilePathHandler.createDirectory(FilePathHandler.getDirectoryPathOnWindows());
                fh = new FileHandler(FilePathHandler.getDirectoryPathOnWindows() + "\\logfile.log");
            } else if (Constants.isPlatformLinux()) {
                FilePathHandler.createDirectory(FilePathHandler.getDirectoryPathOnLinux());
                fh = new FileHandler(FilePathHandler.getDirectoryPathOnLinux() + "/logfile.log");
            }
        } catch (Exception e) {
            System.out.println("creating logfile error");
            //MessageBox.showError("Hiba a naplófájl létrehozásakor!","");
        }
    }
}
