package com.sharedtable.model.signals;

import java.util.UUID;

public class MementoCloserSignal implements Signal {

    public MementoCloserSignal(UUID userID, UUID mementoID, boolean isLinked) {
        this.creatorID = userID;
        this.mementoID = mementoID;
        this.isLinked = isLinked;
    }

    public MementoCloserSignal(String[] input) {
        creatorID = UUID.fromString(input[1]);
        mementoID = UUID.fromString(input[3]);
        isLinked = Boolean.parseBoolean(input[4]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;").append(creatorID.toString()).append(";CLOSE;").append(mementoID.toString()).append(";")
                .append(isLinked);
        return sb.toString();
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


    private UUID creatorID;
    private UUID mementoID;
    private boolean isLinked;


}
