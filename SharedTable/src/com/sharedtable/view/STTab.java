package com.sharedtable.view;

import com.sharedtable.controller.TabController;
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
