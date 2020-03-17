package com.sharedtable.model;

import java.util.UUID;

public class HandshakingInfo {
    public HandshakingInfo(String input) {
        String[] splittedInput = input.split(";");
        if(splittedInput.length != 2) {
            throw new RuntimeException("corrupted handshaking info");
        }
        id = UUID.fromString(splittedInput[0]);
        mementoNumber = Integer.parseInt(splittedInput[1]);
    }

    public HandshakingInfo(UUID id,int mementoNumber) {
        this.id = id;
        this.mementoNumber = mementoNumber;
    }

    public UUID getId() {
        return id;
    }

    public int getMementoNumber() {
        return mementoNumber;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(";").append(mementoNumber).append("\n");
        return sb.toString();
    }

    private UUID id;
    private int mementoNumber;
}
