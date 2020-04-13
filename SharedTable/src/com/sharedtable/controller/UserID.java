package com.sharedtable.controller;


import com.sharedtable.model.Network.UPnP.UPnPConfigException;
import com.sharedtable.model.Network.UPnP.UPnPHandler;
import com.sharedtable.model.Persistence.UserDataPersistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class UserID {

    private UserID() {

    }

    public static void initWithPersistence(UserDataPersistence persistence) {
        userID = persistence.getUserID();
        nickname = persistence.getUserNickname();
        userDataPersistence =persistence;
    }

    public static void initWithoutPersistence(UUID userID, String nickname, String externalIP) {
        UserID.userID = userID;
        UserID.nickname = nickname;
        UserID.IP = externalIP;
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

    public static void setPublicIP(String ip) {IP = ip;}

    public static void setNickname(String nickname) {
        UserID.nickname = nickname;
        userDataPersistence.setUserNickname(nickname);
        try {
            userDataPersistence.writeAllUserData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void setUserID(UUID id) {
        userID = id;
    }

    private static UUID userID = null;
    private static String nickname = "nickname";
    private static String IP = null;
    private static UserDataPersistence userDataPersistence;

}
