package com.sharedtable.model.signals;

import com.sharedtable.model.ArrayPrinter;

import java.util.UUID;

public class NewTabSignal implements Signal {

    public NewTabSignal(UUID creatorID, UUID canvasID, String tabName) {
        this.canvasID = canvasID;
        this.creatorID = creatorID;
        this.tabName = tabName;
    }

    public NewTabSignal(String[] input) {
        if(input.length != 5)
            throw new IllegalArgumentException("NewTabSignal illegal input: "+ ArrayPrinter.printStringArray(input));
        this.creatorID = UUID.fromString(input[2]);
        this.canvasID = UUID.fromString(input[3]);
        this.tabName = input[4];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;NEWTAB;").append(creatorID).append(";")
                .append(canvasID).append(";")
                .append(tabName);
        return sb.toString();
    }

    public UUID getCreatorID() {
        return creatorID;
    }

    public UUID getCanvasID() {
        return canvasID;
    }

    public String getTabName() {
        return tabName;
    }

    UUID creatorID;
    UUID canvasID;
    String tabName;
}
