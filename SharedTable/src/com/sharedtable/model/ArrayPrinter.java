package com.sharedtable.model;

public class ArrayPrinter {

    public static String printStringArray(String[] input) {
        StringBuilder sb = new StringBuilder();
        for(String act :input) {
            sb.append(act).append(";");
        }
        return sb.toString();
    }
}
