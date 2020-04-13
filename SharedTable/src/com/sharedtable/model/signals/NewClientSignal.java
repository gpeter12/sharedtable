package com.sharedtable.model.signals;

import java.util.UUID;

public class NewClientSignal implements Signal {

public NewClientSignal(UUID clientID, String nickname, String IP, int port,
                   int mementoNumber, UUID parentID, int clientBuildNumber)
    {
        this.clientID = clientID;
        this.nickname = nickname;
        this.IP = IP;
        this.port = port;
        this.mementoNumber = mementoNumber;
        this.parentID = parentID;
        this.clientBuildNumber = clientBuildNumber;
    }

    public NewClientSignal(String[] input) {
        clientID = UUID.fromString(input[2]);
        nickname = input[3];
        IP = input[4];
        port = Integer.parseInt(input[5]);
        mementoNumber = Integer.parseInt(input[6]);
        parentID = parentIDFromString(input[7]);
        clientBuildNumber = Integer.parseInt(input[8]);
    }

    private String parentIDToString(UUID parentID) {
        if(parentID == null)
            return "NULL";
        else
            return parentID.toString();
    }

    private UUID parentIDFromString(String parentID) {
        if(parentID.equals( "NULL"))
            return null;
        else
            return UUID.fromString(parentID);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;CONN;").append(clientID).append(";")
                .append(nickname).append(";")
                .append(IP).append(";")
                .append(port).append(";")
                .append(mementoNumber).append(";")
                .append(parentIDToString(parentID)).append(";")
                .append(clientBuildNumber).append(";"); //kell a separator, mert az EntityTreeSignal-ban ezek a stringek egymás után vannak fűzve!!!
        return sb.toString();
    }

    //<editor-fold desc="GET&SET">
    public UUID getClientID() {
        return clientID;
    }

    public void setClientID(UUID clientID) {
        this.clientID = clientID;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMementoNumber() {
        return mementoNumber;
    }

    public void setMementoNumber(int mementoNumber) {
        this.mementoNumber = mementoNumber;
    }

    public UUID getParentID() {
        return parentID;
    }

    public void setParentID(UUID parentID) {
        this.parentID = parentID;
    }

    public int getClientBuildNumber() {
        return clientBuildNumber;
    }

    public void setClientBuildNumber(int clientBuildNumber) {
        this.clientBuildNumber = clientBuildNumber;
    }
    //</editor-fold>



    private UUID clientID;
    private String nickname;
    private String IP;
    private int port;
    private int mementoNumber;
    private UUID parentID;
    private int clientBuildNumber;
}
