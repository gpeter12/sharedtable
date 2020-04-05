package com.sharedtable.model;

import java.util.ArrayList;

public class ArrayPrinter {

    public static String printStringArray(String[] input) {
        StringBuilder sb = new StringBuilder();
        for(String act :input) {
            sb.append(act).append(";");
        }
        return sb.toString();
    }

    public static String printStringArray(ArrayList<String> input) {
        StringBuilder sb = new StringBuilder();
        for(String act :input) {
            sb.append(act).append(";");
        }
        return sb.toString();
    }
}
