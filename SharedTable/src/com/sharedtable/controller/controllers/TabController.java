package com.sharedtable.controller.controllers;

import com.sharedtable.controller.KeyboardEventHandler;
import com.sharedtable.controller.UserID;
import com.sharedtable.model.NetworkService;
import com.sharedtable.model.signals.CloseTabSignal;
import com.sharedtable.model.signals.NewTabSignal;
import com.sharedtable.view.STTabPane;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.UUID;

public class TabController {

    public TabController(STTabPane stTabPane, Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.stTabPane = stTabPane;
        createNewTab(UUID.fromString("00000000-0000-0000-0000-000000000000"),"newTab");
    }

    public static void createNewTab(UUID canvasID, String tabName) {
        UUID tabid = canvasID;

        stTabPane.createNewTab(tabid,tabName);
        CanvasController canvasController = new CanvasController(stTabPane.getCanvas(tabid),tabid);
        initKeyboardEventHandler(primaryStage,canvasController);
        canvasControllers.add(canvasController);

    }

    public static void initKeyboardEventHandler(Stage primaryStage, CanvasController canvasController){
        KeyboardEventHandler keyboardEventHandler = new KeyboardEventHandler(canvasController);
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED,
                event -> {
                    keyboardEventHandler.handleEvent(event);
                });
    }

    public static void onTabClosed(UUID tabID) {
        removeTab(tabID);
        NetworkService.sendCloseTabSignal(UserID.getUserID(),tabID);
    }

    public static void removeTab(UUID tabId) {
        removeCanvasController(tabId);
    }

    public static void stop() {
        for(CanvasController act : canvasControllers) {
            act.stop();
        }
    }

    public static CanvasController getActualCanvasControler() {
        return getCanvasController(stTabPane.getSelectedTabID());
    }

    public static int getMementoCountOnAllCanvasController() {
        int sum=0;
        for(CanvasController act : canvasControllers) {
            sum += act.getMementos().size();
        }
        return sum;
    }

    public static void handleNewTabSingal(NewTabSignal newTabSignal) {
        Platform.runLater(() -> {
            createNewTab(newTabSignal.getCanvasID(),newTabSignal.getTabName());
        });
    }

    public static void handleCloseTabSignal(CloseTabSignal closeTabSignal) {
        Platform.runLater(() -> {
            removeTab(closeTabSignal.getCanvasID());
            stTabPane.removeTab(closeTabSignal.getCanvasID());
                });
    }

///////////////////////////////////////
    private static void removeCanvasController(UUID id) {
        canvasControllers.remove(getCanvasController(id));
    }

    public static CanvasController getCanvasController(UUID id) {
        for(CanvasController act : canvasControllers) {
            if(act.getCanvasID().equals(id))
                return act;
        }
        throw new RuntimeException("canvas controller not found!");
    }

    private static Stage primaryStage;
    private static ArrayList<CanvasController> canvasControllers = new ArrayList<>();
    private static STTabPane stTabPane;


}
