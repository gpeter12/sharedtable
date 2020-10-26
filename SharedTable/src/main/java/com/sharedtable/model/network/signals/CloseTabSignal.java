package com.sharedtable.model.network.signals;

import com.sharedtable.Utils;

import java.util.UUID;

public class CloseTabSignal implements Signal {


    private UUID creatorID;
    private UUID canvasID;

    public CloseTabSignal(UUID creatorID, UUID canvasID) {
        this.creatorID = creatorID;
        this.canvasID = canvasID;
    }

    public CloseTabSignal(String[] input) {
        if(input.length != 4)
            throw new IllegalArgumentException("CloseTabSignal illegal input: "+ Utils.recombineStringArray(input));
        this.creatorID = UUID.fromString(input[2]);
        this.canvasID = UUID.fromString(input[3]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;CLOSETAB;").append(creatorID).append(";")
                .append(canvasID);
        return sb.toString();
    }

    public UUID getCreatorID() {
        return creatorID;
    }

    public UUID getCanvasID() {
        return canvasID;
    }


}