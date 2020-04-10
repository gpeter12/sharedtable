package com.sharedtable.controller;

import java.util.logging.Logger;

public class Sleep {

    public static void sleep(int time, Logger logger) {

        try {
            logger.info("sleeping "+time);
            Thread.sleep(time);
        } catch (Exception e) { logger.severe("Sleep fail!"); }
    }

}
