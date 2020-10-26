package com.sharedtable.model.network.signals;

import com.sharedtable.Utils;

import java.util.UUID;

public class MementoCloserSignal implements Signal {

    private UUID canvasID;
    private UUID creatorID;
    private UUID mementoID;
    private boolean isLinked;
    private UUID prevMementoID;
    private UUID nextMementoID;

    public MementoCloserSignal(UUID userID, UUID canvasID,UUID mementoID, boolean isLinked, UUID prevMementoID,UUID nextMementoID) {
        this.creatorID = userID;
        this.mementoID = mementoID;
        this.isLinked = isLinked;
        this.canvasID = canvasID;
        this.prevMementoID = Utils.convertNullUUIDToNil(prevMementoID);
        this.nextMementoID = Utils.convertNullUUIDToNil(nextMementoID);
    }



    public MementoCloserSignal(String[] input) {
        if(input.length != 8)
            throw new IllegalArgumentException("illegal MementoCloserSignal input array: "+ Utils.recombineStringArray(input));
        creatorID = UUID.fromString(input[1]);
        mementoID = UUID.fromString(input[3]);
        canvasID = UUID.fromString(input[4]);
        isLinked = Boolean.parseBoolean(input[5]);
        prevMementoID = UUID.fromString(input[6]);
        nextMementoID = UUID.fromString(input[7]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;").append(creatorID.toString()).append(";CLOSE;")
                .append(mementoID.toString()).append(";")
                .append(canvasID).append(";")
                .append(isLinked).append(";")
                .append(prevMementoID).append(";")
                .append(nextMementoID);
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

    public boolean isLinked() {
        return isLinked;
    }

    public UUID getPrevMementoID() {
        return prevMementoID;
    }

    public UUID getNextMementoID() {
        return nextMementoID;
    }
}
