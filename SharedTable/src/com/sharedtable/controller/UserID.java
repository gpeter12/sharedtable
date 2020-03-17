package com.sharedtable.controller;

import java.util.UUID;

public class UserID {

    private UserID() {

    }

    public static UUID getUserID() {
        if (userID == null)
            userID = UUID.randomUUID();
        return userID;
    }

    private static UUID userID = null;

}
