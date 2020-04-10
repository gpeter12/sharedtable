package com.sharedtable.controller;

import com.sharedtable.UPnP.UPnP;

import java.util.UUID;

public class UserID {

    private UserID() {

    }

    public static UUID getUserID() {
        return userID;
    }

    public static String getNickname() {
        return nickname;
    }

    public static String getPublicIP() {
        return IP;
    }

    private static UUID generateUserID() {
        UUID uuid = UUID.randomUUID();
        System.out.println("MY USR ID: "+uuid.toString());
        System.out.println("-----------------");
        return uuid;
    }

    private static String getPublicIPFromRouter() {
        String res = UPnP.getExternalIP();
        if(res == null)
            return "UPnP Unsupported";
        return res;
    }

    private static final UUID userID = generateUserID();
    private static final String nickname = "nickname";
    private static final String IP = getPublicIPFromRouter();
}
