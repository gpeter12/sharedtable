package com.sharedtable.model;

import java.util.UUID;

public class SignalFactory {

    public static String getNewRootSignal(UUID newRootID){
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;NEWROOT;").append(newRootID);
        return sb.toString();
    }

    public static String getNewClientSignal(UUID clientID, String nickname, String IP, int port, int mementoNumber, UUID parent) {
        StringBuilder sb = new StringBuilder();
        String parentID = "NULL";
        if(parent != null)
            parentID = parent.toString();

        sb.append("SIG;CONN;").append(clientID).append(";").append(nickname).append(";").append(IP).
                append(";").append(port).append(";").append(mementoNumber).append(";").append(parentID);
        return sb.toString();
    }

    public static String getNewDisconnectSignal(UUID clientID,String nickname,String IP) {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;DISCONN;").append(clientID).append(";").append(nickname).append(";").append(IP);
        return sb.toString();
    }


    public static String getMementoCloserSignal(UUID userID,UUID mementoID,boolean isLinked) {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;").append(userID.toString()).append(";CLOSE;").append(mementoID.toString()).append(";")
                .append(isLinked);
        return sb.toString();
    }

    public static String getMementoOpenerSignal(UUID userID,UUID mementoID,boolean isLinked) {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;").append(userID.toString()).append(";OPEN;").append(mementoID.toString()).append(";")
                .append(isLinked);
        return sb.toString();
    }





























}
