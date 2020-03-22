package com.sharedtable.model;

import java.util.Objects;
import java.util.UUID;

public class NetworkClientEntity {

    public NetworkClientEntity(UUID uuid,String nickname,String IP,int mementoNumber) {
        this.id = uuid;
        this.IP = IP;
        this.nickname = nickname;
        this.mementoNumber = mementoNumber;
    }

    public NetworkClientEntity(String[] input) {
        String[] splittedInput = input;
        if(splittedInput.length != 4) {
            throw new RuntimeException("corrupted handshaking info");
        }
        id = UUID.fromString(splittedInput[0]);
        nickname = splittedInput[1];
        IP = splittedInput[2];
        mementoNumber = Integer.parseInt(input[3]);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id.toString()).append(";").append(nickname).append(";").append(IP).
                append(";").append(mementoNumber);
        return sb.toString();
    }

    public UUID getID() {return id;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkClientEntity that = (NetworkClientEntity) o;
        return Objects.equals(id, that.id);
    }

    public String getIP() {
        return IP;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private UUID id;
    private String IP;
    private String nickname;
    private int mementoNumber;
}
