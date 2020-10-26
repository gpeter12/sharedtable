package com.sharedtable;

import java.util.ArrayList;
import java.util.UUID;

public class Utils {

    public static String nullUUIDConverter(String uuid) {
        if(uuid.equals("null"))
            return "00000000-0000-0000-0000-000000000000";
        return uuid;
    }

    public static UUID convertNullUUIDToNil(UUID inp) {
        if(inp == null)
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        return inp;
    }



    public static String recombineStringArray(String[] input) {
        StringBuilder sb = new StringBuilder();
        for(String act :input) {
            sb.append(act).append(";");
        }
        return sb.toString();
    }

    public static String recombineStringArray(ArrayList<String> input) {
        StringBuilder sb = new StringBuilder();
        for(String act :input) {
            sb.append(act).append(";");
        }
        return sb.toString();
    }
}
