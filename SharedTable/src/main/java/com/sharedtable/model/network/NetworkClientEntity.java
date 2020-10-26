package com.sharedtable.model.network;

import com.sharedtable.Constants;
import com.sharedtable.Utils;

import java.util.Objects;
import java.util.UUID;

public class NetworkClientEntity {

    //private NetworkClientEntity upperClientEntity;
    private UUID upperClientID;
    private UUID id;
    private String IP;
    private int port;
    private String nickname;
    private int clientBuildNumber;
    private int mementoNumber;
    private String platform;

    public NetworkClientEntity(UUID uuid, String nickname, String IP, int port,
                               int mementoNumber,UUID parentID, int clientBuildNumber)
    {
        this.id = uuid;
        this.IP = IP;
        this.port = port;
        this.nickname = nickname;
        this.mementoNumber = mementoNumber;
        this.upperClientID = parentID;
        this.clientBuildNumber = clientBuildNumber;
        this.platform = Constants.getPlatformString();
    }

    public NetworkClientEntity(String[] input) {
        String[] splittedInput = input;
        if(!input[0].equals("HSI")) {
            throw new IllegalArgumentException("corrupted handshaking info! length:"+splittedInput.length+" :"+ Utils.recombineStringArray(splittedInput));
        }
        id = UUID.fromString(splittedInput[1]);
        nickname = splittedInput[2];
        IP = splittedInput[3];
        port = Integer.parseInt(splittedInput[4]);
        mementoNumber = Integer.parseInt(input[5]);
        upperClientID = parentIDFromString(input[6]);
        clientBuildNumber = Integer.parseInt(input[7]);
        platform = input[8];
    }

    private String parentIDToString(UUID parentID) {
        if(parentID == null)
            return "NULL";
        else
            return parentID.toString();
    }

    private UUID parentIDFromString(String parentID) {
        if(parentID.equals("NULL"))
            return null;
        else
            return UUID.fromString(parentID);
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HSI").append(";")
                .append(id.toString()).append(";")
                .append(nickname).append(";")
                .append(IP).append(";")
                .append(port).append(";")
                .append(mementoNumber).append(";")
                .append(parentIDToString(upperClientID)).append(";")
                .append(clientBuildNumber).append(";")
                .append(platform).append(";"); //kell a separator, mert az EntityTreeSignal-ban ezek a stringek egymás után vannak fűzve!!!
        System.out.println("platform string sent: "+platform);
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
    {
        if(upperClientEntity != null)
            this.upperClientID = upperClientEntity.id;
        else
            this.upperClientID = null;
    }

    public void setMementoNumber(int mementoNumber) {
        this.mementoNumber = mementoNumber;
    }
    public void setPort(int i) {
        port = i;
    }
    public int getClientBuildNumber() {return clientBuildNumber;}
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getPlatform() {
        return platform;
    }
    public boolean isPlatformLinux() {return platform.equals("Linux");}
    public boolean isPlatformWindows() {return platform.equals("Windows");}



}
