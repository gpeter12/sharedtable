package com.sharedtable.model.network.signals;

import java.util.UUID;

public class PingSignal implements Signal{

    private UUID pingID;
    private UUID creatorID;
    private UUID targetClientID;
    private boolean isResponse;

    public PingSignal(UUID creatorID, UUID targetClientID, boolean isResponse) {
        this.creatorID = creatorID;
        this.targetClientID = targetClientID;
        this.isResponse = isResponse;
        pingID = UUID.randomUUID();
    }

    public PingSignal(String[] input) {
        creatorID = UUID.fromString(input[2]);
        targetClientID = UUID.fromString(input[3]);
        isResponse = Boolean.parseBoolean(input[4]);
        pingID = UUID.fromString(input[5]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;PING;").append(creatorID.toString()).append(";")
                .append(targetClientID.toString()).append(";")
                .append(isResponse).append(";")
                .append(pingID);
        return sb.toString();
    }

    public UUID getCreatorID() {
        return creatorID;
    }

    public UUID getTargetClientID() {
        return targetClientID;
    }

    public boolean isResponse() {
        return isResponse;
    }

    public UUID getPingID() {
        return pingID;
    }

    public void setResponse(boolean response) {
        isResponse = response;
    }


}
