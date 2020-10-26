package com.sharedtable.model.network.signals;

import java.util.UUID;

public class DiscoverySignal implements Signal {

    private UUID creatorID;


    public DiscoverySignal(UUID creatorID) {
        this.creatorID = creatorID;
    }

    public DiscoverySignal(String[] input) {
        creatorID = UUID.fromString(input[2]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;DISCOV;").append(creatorID.toString());
        return sb.toString();
    }

    public UUID getCreatorID() {
        return creatorID;
    }

}
