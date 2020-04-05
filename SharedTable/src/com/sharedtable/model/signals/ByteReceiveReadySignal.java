package com.sharedtable.model.signals;

import java.util.UUID;

public class ByteReceiveReadySignal implements Signal {

    public ByteReceiveReadySignal(UUID CreatorID, UUID receiverID) {
        this.creatorID = CreatorID;
        this.receiverID = receiverID;
    }

    ByteReceiveReadySignal(String [] input) {
        this.creatorID = UUID.fromString(input[2]);
        this.receiverID = UUID.fromString(input[3]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;BRECREADY;").append(creatorID).append(";")
                .append(receiverID);
        return sb.toString();
    }

    public UUID getCreatorID() {
        return creatorID;
    }

    public UUID getReceiverID() {
        return receiverID;
    }

    private UUID creatorID;
    private UUID receiverID;
}
