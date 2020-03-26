package com.sharedtable.model.signals;

import java.util.UUID;

public class DisconnectSignal implements Signal {

    public DisconnectSignal(UUID clientID, String nickname, String IP) {
        this.clientID = clientID;
        this.nickname = nickname;
        this.IP = IP;
    }

    public DisconnectSignal(String[] input) {
        clientID = UUID.fromString(input[2]);
        nickname = input[3];
        IP = input[4];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;DISCONN;").append(clientID).append(";").
                append(nickname).append(";").
                append(IP);
        return sb.toString();
    }

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

    private UUID clientID;
    private String nickname;
    private String IP;
}
