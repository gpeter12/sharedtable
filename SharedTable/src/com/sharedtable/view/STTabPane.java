package com.sharedtable.view;


import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.UUID;

public class STTabPane extends TabPane {

    public STTabPane() {
    }


    public UUID createNewTab(UUID canvasID, String tabName) {
        UUID tabID = canvasID;
        STTab newTab = new STTab();

        newTab.setId(tabID.toString());
        newTab.setContent(new STCanvas());
        newTab.setText(tabName);
        getTabs().add(newTab);
        return tabID;
    }

    public void removeTab(UUID tabID) {
        getTabs().remove(getTab(tabID));
    }

    public Tab getTab(UUID tabID) {
        for(Tab act : getTabs()) {
            if(act.getId().equals(tabID.toString()))
                return act;
        }
        throw new RuntimeException("tab not found!");
    }

    public UUID getSelectedTabID() {
        return UUID.fromString(getSelectionModel().getSelectedItem().getId());
    }

    public STCanvas getCanvas(UUID tabID) {
        return (STCanvas)getTab(tabID).getContent();
    }
}
