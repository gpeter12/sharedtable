package com.sharedtable.model.network.signals;

import java.util.UUID;

public class RenameTabSignal extends NewTabSignal {

    public RenameTabSignal(UUID creatorID, UUID canvasID, String newName) {
        super(creatorID,canvasID,newName);
    }

    public RenameTabSignal(String[] input) {
        super(input);
    }

    @Override
    public String toString() {
        return super.toString().replace("NEWTAB","TABRENAME");
    }
}
