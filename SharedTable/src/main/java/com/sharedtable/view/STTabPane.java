package com.sharedtable.view;


import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.ArrayList;
import java.util.UUID;

public class STTabPane extends TabPane {

    public STTabPane() {
    }


    public UUID createNewTab(UUID canvasID, String tabName) {
        UUID tabID = canvasID;
        STTab newTab = new STTab();

        newTab.setId(tabID.toString());
        STCanvas stCanvas = new STCanvas();

        //stCanvas.heightProperty().bind(this.heightProperty());
        //stCanvas.widthProperty().bind(this.widthProperty());

        newTab.setContent(stCanvas);
        newTab.setText(tabName);
        getTabs().add(newTab);
        return tabID;
    }

    public void renameTab(UUID tabID, String tabName) {
        Platform.runLater(() -> {
            getTab(tabID).setText(tabName);
        });

    }

    public void removeTab(UUID tabID) {
        getTabs().remove(getTab(tabID));
    }

    public ArrayList<STTab> getAllTabs() {
        ArrayList<STTab> res = new ArrayList<>();
        for(Tab act : getTabs()) {
            res.add((STTab)act);
        }
        return res;
    }

    public Tab getTab(UUID tabID) {
        for(Tab act : getTabs()) {
            if(act.getId().equals(tabID.toString()))
                return act;
        }
        throw new RuntimeException("tab not found!");
    }

    public void setClosable(boolean val) {
        for(STTab act : getAllTabs()) {
            act.setClosable(val);
        }
    }

    public UUID getSelectedTabID() {
        return UUID.fromString(getSelectionModel().getSelectedItem().getId());
    }

    public STCanvas getCanvas(UUID tabID) {
        return (STCanvas)getTab(tabID).getContent();
    }


}
