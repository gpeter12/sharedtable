package com.sharedtable.model.signals;

import com.sharedtable.model.ArrayPrinter;

import java.util.UUID;

public class MementoCloserSignal implements Signal {

    public MementoCloserSignal(UUID userID, UUID canvasID,UUID mementoID, boolean isLinked) {
        this.creatorID = userID;
        this.mementoID = mementoID;
        this.isLinked = isLinked;
        this.canvasID = canvasID;
    }

    public MementoCloserSignal(String[] input) {
        if(input.length != 6)
            throw new IllegalArgumentException("illegal MementoCloserSignal input array: "+ ArrayPrinter.printStringArray(input));
        creatorID = UUID.fromString(input[1]);
        mementoID = UUID.fromString(input[3]);
        canvasID = UUID.fromString(input[4]);
        isLinked = Boolean.parseBoolean(input[5]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;").append(creatorID.toString()).append(";CLOSE;")
                .append(mementoID.toString()).append(";")
                .append(canvasID).append(";")
                .append(isLinked);
        return sb.toString();
    }

    public UUID getCanvasID() {
        return canvasID;
    }

    public UUID getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(UUID creatorID) {
        this.creatorID = creatorID;
    }

    public UUID getMementoID() {
        return mementoID;
    }

    public void setMementoID(UUID mementoID) {
        this.mementoID = mementoID;
    }

    public boolean isLinked() {
        return isLinked;
    }

    public void setLinked(boolean linked) {
        isLinked = linked;
    }

    private UUID canvasID;
    private UUID creatorID;
    private UUID mementoID;
    private boolean isLinked;


}
