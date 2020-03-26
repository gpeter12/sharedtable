package com.sharedtable.model.signals;

import java.util.UUID;

public class PingSignal implements Signal{

    public PingSignal(UUID creatorID, UUID targetClientID, boolean isRespond) {
        this.creatorID = creatorID;
        this.targetClientID = targetClientID;
        this.isRespond = isRespond;
        pingID = UUID.randomUUID();
    }

    public PingSignal(String[] input) {
        creatorID = UUID.fromString(input[2]);
        targetClientID = UUID.fromString(input[3]);
        isRespond = Boolean.parseBoolean(input[4]);
        pingID = UUID.fromString(input[5]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;PING;").append(creatorID.toString()).append(";")
                .append(targetClientID.toString()).append(";")
                .append(isRespond).append(";")
                .append(pingID);
        return sb.toString();
    }

    public UUID getCreatorID() {
        return creatorID;
    }

    public UUID getTargetClientID() {
        return targetClientID;
    }

    public boolean isRespond() {
        return isRespond;
    }

    public UUID getPingID() {
        return pingID;
    }

    public void setRespond(boolean respond) {
        isRespond = respond;
    }

    private UUID pingID;
    private UUID creatorID;
    private UUID targetClientID;
    private boolean isRespond;
}
