package com.sharedtable.model.signals;

import java.util.UUID;

public class SyncedSignal implements Signal {

    public SyncedSignal(UUID creatorID) {
        this.creatorID = creatorID;
    }


    public SyncedSignal(String[] input) {
        creatorID = UUID.fromString(input[2]);

    }

    public UUID getCreatorID() {
        return creatorID;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;SYNCED;").append(creatorID);
        return sb.toString();
    }

    private UUID creatorID;


}
