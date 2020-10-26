package com.sharedtable.model.network.signals;

import java.util.UUID;

public class MementoOpenerSignal implements Signal {

    private UUID canvasID;
    private UUID creatorID;

    public MementoOpenerSignal(UUID userID, UUID canvasID) {
        this.creatorID = userID;
        this.canvasID = canvasID;
    }

    public MementoOpenerSignal(String[] input) {
        creatorID = UUID.fromString(input[1]);
        canvasID = UUID.fromString(input[3]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;").append(creatorID.toString()).append(";OPEN;").
                append(canvasID.toString());

        return sb.toString();
    }

    //<editor-fold desc="GET&SET">

    public UUID getCanvasID() {
        return canvasID;
    }

    public UUID getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(UUID creatorID) {
        this.creatorID = creatorID;
    }

    //</editor-fold>


}
