package com.sharedtable.controller;

import java.util.UUID;

public class UserID {

    private UserID() {

    }

    public static UUID getUserID() {
        return userID;
    }

    private static UUID generateUserID() {
        UUID uuid = UUID.randomUUID();
        System.out.println("MY USR ID: "+uuid.toString());
        System.out.println("-----------------");
        return uuid;
    }

    private static UUID userID = generateUserID();

}
