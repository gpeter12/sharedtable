package com.sharedtable.model.network.signals;

import com.sharedtable.Constants;
import com.sharedtable.Utils;

import java.util.UUID;

public class NetworkPasswordChangeSignal implements Signal {

    private UUID creatorID;
    private String password;

    public NetworkPasswordChangeSignal(UUID creatorID, String password) {
        this.creatorID = creatorID;
        this.password = password;
    }

    public NetworkPasswordChangeSignal(String[] input) {
        if(input.length != 4) {
            throw new IllegalArgumentException("NetworkPasswordChangeSignal illegal input: "+ Utils.recombineStringArray(input));
        }
        this.creatorID = UUID.fromString(input[2]);
        this.password = input[3];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;PASSWD;").append(creatorID).append(";")
                .append(getPassword());
        return sb.toString();
    }

    public UUID getCreatorID() {
        return creatorID;
    }

    public String getPassword() {
        if(password.isEmpty()) {
            return Constants.getNoPasswordConstant();
        }
        return password;
    }


}
