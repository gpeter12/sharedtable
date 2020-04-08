package com.sharedtable.controller;

public class Sleep {

    public static void sleep(int time) {
        System.out.println("sleeping "+time );
        try { Thread.sleep(time); } catch (Exception e) { System.out.println("Sleep fail!"); }
    }
}
