package com.sharedtable.model.signals;

import com.sharedtable.model.ArrayPrinter;

import java.util.UUID;

public class NetworkPasswordChangeSignal implements Signal {

    public NetworkPasswordChangeSignal(UUID creatorID, String password) {
        this.creatorID = creatorID;
        this.password = password;
    }

    public NetworkPasswordChangeSignal(String[] input) {
        if(input.length != 4) {
            throw new IllegalArgumentException("NetworkPasswordChangeSignal illegal input: "+ ArrayPrinter.printStringArray(input));
        }
        this.creatorID = UUID.fromString(input[2]);
        this.password = input[3];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;PASSWD;").append(creatorID).append(";")
                .append(password);
        return sb.toString();
    }

    public UUID getCreatorID() {
        return creatorID;
    }

    public String getPassword() {
        return password;
    }

    private UUID creatorID;
    private String password;
}
