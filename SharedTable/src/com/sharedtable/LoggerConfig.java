package com.sharedtable;

import com.sharedtable.model.Persistence.FilePathHandler;
import com.sharedtable.view.MessageBox;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerConfig {

    public static Logger setLogger(Logger logger){

        logger.addHandler(fh);
        logger.setLevel(Level.FINEST);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        logger.getHandlers()[0].setLevel(Level.FINEST);

        return logger;
    }

    private static FileHandler fh=null;

    static  {
        try {
            if (FilePathHandler.isPlatformWindows()) {
                fh = new FileHandler(FilePathHandler.getDirectoryPathOnWindows() + "\\logfile.log");
            } else if (FilePathHandler.isPlatformLinux()) {
                fh = new FileHandler(FilePathHandler.getDirectoryPathOnLinux() + "/logfile.log");
            }
        } catch (Exception e) {
            MessageBox.showError("Hiba a naplófájl létrehozásakor!","");
        }
    }
}
