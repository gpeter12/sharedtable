package com.sharedtable.controller;


import com.sharedtable.model.Network.UPnP.UPnPConfigException;
import com.sharedtable.model.Network.UPnP.UPnPHandler;
import com.sharedtable.model.Persistence.UserDataPersistence;

import java.io.IOException;
import java.util.UUID;

public class UserID {

    private UserID() {

    }

    public static void setPersistence(UserDataPersistence persistence) {
        userID = persistence.getUserID();
        nickname = persistence.getUserNickname();
        userDataPersistence =persistence;
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

    public static void setNickname(String nickname) {
        UserID.nickname = nickname;
        userDataPersistence.setUserNickname(nickname);
        try {
            userDataPersistence.writeAllUserData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getPublicIPFromRouter() {
        String res;
        try {
            res = UPnPHandler.getExternalIP();
        } catch (UPnPConfigException e) {
            return "UPnP Unsupported";
        }
        return res;
    }

    public static void setUserID(UUID id) {
        userID = id;
    }

    private static UUID userID = null;
    private static String nickname = "nickname";
    private static final String IP = getPublicIPFromRouter();
    private static UserDataPersistence userDataPersistence;
}
