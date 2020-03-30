package com.sharedtable.view;

import com.sharedtable.controller.controllers.TabController;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;

import java.util.UUID;

public class STTab extends Tab {

    public STTab() {
        super();
        setOnClosed(event -> {
            onClosed();
        });
    }

    private void onClosed() {
        TabController.onTabClosed(UUID.fromString(getId()));
    }


}
