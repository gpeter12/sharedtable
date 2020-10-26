package com.sharedtable.model.network.signals;

import java.util.UUID;

public class DisconnectSignal implements Signal {

    private UUID clientID;
    private String nickname;

    public DisconnectSignal(UUID clientID, String nickname) {
        this.clientID = clientID;
        this.nickname = nickname;
    }

    public DisconnectSignal(String[] input) {
        clientID = UUID.fromString(input[2]);
        nickname = input[3];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;DISCONN;").append(clientID).append(";").
                append(nickname);
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

}
