package com.sharedtable.controller;


import com.sharedtable.model.persistence.UserDataPersistence;

import java.io.IOException;
import java.util.UUID;

public class UserID {

    public void setUserID(UUID id) {
        userID = id;
    }

    private UUID userID = null;
    private String nickname = "nickname";
    private String IP = null;
    private static UserID instance = new UserID();

    private UserID() {

    }

    public static UserID getInstance() {
        if(instance == null) {
            instance = new UserID();
        }
        return instance;
    }

    public void initWithPersistence() {
        UserDataPersistence persistence = UserDataPersistence.getInstance();
        userID = persistence.getUserID();
        nickname = persistence.getUserNickname();
    }

    public void initWithoutPersistence(UUID userID, String nickname, String externalIP) {
        this.userID = userID;
        this.nickname = nickname;
        this.IP = externalIP;
    }

    public UUID getUserID() {
        return userID;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPublicIP() {
        return IP;
    }

    public void setPublicIP(String ip) {IP = ip;}

    public void setNickname(String nickname) {
        this.nickname = nickname;
        UserDataPersistence.getInstance().setUserNickname(nickname);
        try {
            UserDataPersistence.getInstance().writeAllUserData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





}
