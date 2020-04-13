package com.sharedtable.controller;

import com.sharedtable.Constants;
import com.sharedtable.view.MainView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Logger;

public class UpdateChecker {

    public UpdateChecker(String buildNumberFileURL) {
        logger = Logger.getLogger(MainView.class.getName());
        try {
            URL url = new URL(buildNumberFileURL);
            downloadedBuildNumber = getBuildNumber(url);
        } catch (MalformedURLException e) {
            logger.warning("invalid input URL: "+buildNumberFileURL);
            e.printStackTrace();
        } catch (IOException e) {
            logger.warning("can't open buildNumberFile");
            e.printStackTrace();
        }
    }

    private int getBuildNumber(URL url) throws IOException {
        Scanner scanner = new Scanner(url.openStream());
        return Integer.parseInt(scanner.nextLine());
    }

    public boolean isUpdateAvailable() {
        if(downloadedBuildNumber > Constants.getBuildNumber())
            return true;
        return false;
    }

    private int downloadedBuildNumber = Constants.getBuildNumber();
    private Logger logger;
}
