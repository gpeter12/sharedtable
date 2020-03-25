package com.sharedtable.model.signals;

import java.util.UUID;

public class RootSignal implements Signal {

    public RootSignal(UUID newRootID) {
        this.newRootID = newRootID;
    }

    public RootSignal(String[] input) {
        newRootID = UUID.fromString(input[2]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;NEWROOT;").append(newRootID);
        return sb.toString();
    }

    public UUID getNewRootID() {
        return newRootID;
    }

    public void setNewRootID(UUID newRootID) {
        this.newRootID = newRootID;
    }

    private UUID newRootID;
}
