package com.sharedtable.model;

import java.util.Objects;
import java.util.UUID;

public class NetworkClientEntity {

    public NetworkClientEntity(UUID uuid, String nickname, String IP, int port, int mementoNumber) {
        this.id = uuid;
        this.IP = IP;
        this.port = port;
        this.nickname = nickname;
        this.mementoNumber = mementoNumber;
    }

    public NetworkClientEntity(String[] input) {
        String[] splittedInput = input;
        if(splittedInput.length != 5) {
            throw new RuntimeException("corrupted handshaking info");
        }
        id = UUID.fromString(splittedInput[0]);
        nickname = splittedInput[1];
        IP = splittedInput[2];
        port = Integer.parseInt(splittedInput[3]);
        mementoNumber = Integer.parseInt(input[4]);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id.toString()).append(";").append(nickname).append(";").append(IP).
                append(";").append(port).append(";").append(mementoNumber);
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkClientEntity that = (NetworkClientEntity) o;
        return Objects.equals(id, that.id);
    }

    public UUID getID() {return id;}
    public String getIP() { return IP; }
    public int getPort() { return port; }
    public void setIP(String IP) {
        this.IP = IP;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public int getMementoNumber() {return mementoNumber;}
    public boolean hasOpenedPort() {return port != -1;}
    public void setUpperClientID(UUID id) {upperClientID = id;}
    public UUID getUpperClientID() {return upperClientID;}
    public void setUpperClientEntity(NetworkClientEntity upperClientEntity)
    { this.upperClientEntity = upperClientEntity; }
    public NetworkClientEntity getUpperClientEntity() { return upperClientEntity; }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private NetworkClientEntity upperClientEntity;
    private UUID upperClientID;
    private UUID id;
    private String IP;
    private int port;
    private String nickname;
    private int mementoNumber;
}
