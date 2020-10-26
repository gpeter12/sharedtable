package com.sharedtable.model.network.signals;

import com.sharedtable.Utils;

import java.util.UUID;

public class DeleteAfterSignal implements Signal {

    private UUID creatorID;
    private UUID mementoID;
    private UUID canvasID;

    public DeleteAfterSignal(UUID creatorID, UUID mementoID, UUID canvasID) {
        this.creatorID = creatorID;
        this.mementoID = mementoID;
        this.canvasID = canvasID;
    }

    public DeleteAfterSignal(String[] input) {
        System.out.println(Utils.recombineStringArray(input));
        creatorID = UUID.fromString(input[2]);
        mementoID = UUID.fromString(Utils.nullUUIDConverter(input[3]));
        canvasID = UUID.fromString(input[4]);
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;DELAFTER;").append(creatorID.toString())
        .append(";").append(mementoID)
        .append(";").append(canvasID);
        return sb.toString();
    }

    public UUID getCreatorID() {
        return creatorID;
    }
    public UUID getMementoID() {return mementoID;}
    public UUID getCanvasID() {return canvasID;}
}
