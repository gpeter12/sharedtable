package com.sharedtable.controller;

import com.sharedtable.controller.commands.Command;
import com.sharedtable.controller.commands.DrawImageCommand;
import com.sharedtable.model.NetworkService;
import com.sharedtable.model.signals.CloseTabSignal;
import com.sharedtable.model.signals.NewTabSignal;
import com.sharedtable.view.STTab;
import com.sharedtable.view.STTabPane;
import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.UUID;

public class TabController {

    public TabController(STTabPane stTabPane, Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.stTabPane = stTabPane;
        createNewTab(UUID.randomUUID(),"newTab");
    }

    public static void createNewTab(UUID canvasID, String tabName) {
        UUID tabid = canvasID;
        stTabPane.createNewTab(tabid,tabName);
        CanvasController canvasController = new CanvasController(stTabPane.getCanvas(tabid),tabid);
        initKeyboardEventHandler(primaryStage,canvasController);
        canvasControllers.add(canvasController);

    }

    public static boolean hasCanvas(UUID id) {
        for(CanvasController act : canvasControllers) {
            if(act.getCanvasID().equals(id))
                return true;
        }
        return false;
    }

    public static ArrayList<CanvasController> getAllCanvasControllers() {
        return canvasControllers;
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
        getCanvasController(tabId).stop();
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

    public static ArrayList<NewTabSignal> generateNewTabSignalsFromAllTab() {
        ArrayList<NewTabSignal> signals = new ArrayList<>();
        for(STTab act : stTabPane.getAllTabs()) {
            signals.add(new NewTabSignal(UserID.getUserID(),
                    getCanvasController(UUID.fromString(act.getId())).getCanvasID(),
                    act.getText()));
        }
        return signals;
    }

    public static void handleNewTabSingal(NewTabSignal newTabSignal) {
        Platform.runLater(() -> {
            if(hasCanvas(newTabSignal.getCanvasID()))
                return;
            createNewTab(newTabSignal.getCanvasID(),newTabSignal.getTabName());
        });
    }

    public static void handleCloseTabSignal(CloseTabSignal closeTabSignal) {
        Platform.runLater(() -> {
            removeTab(closeTabSignal.getCanvasID());
            stTabPane.removeTab(closeTabSignal.getCanvasID());
                });
    }

    public static byte[] getAllBytesFromAllImages() {
        ArrayList<Byte> resList = new ArrayList<>();
        for(CanvasController actController : TabController.getAllCanvasControllers()) {
            for(StateMemento actMemento : actController.getMementos()) {
                for(Command actCommand : actMemento.getCommands()) {
                    if(actCommand instanceof DrawImageCommand) {
                        DrawImageCommand drawImageCommand = (DrawImageCommand)actCommand;
                        for(byte actByte : drawImageCommand.getImageBytes()) {
                            resList.add(actByte);
                        }
                    }
                }
            }
        }
        byte[] resArray = new byte[resList.size()];
        for(int i = 0; i<resArray.length; i++) {
            resArray[i] = resList.get(i);
        }
        return resArray;
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
        System.out.println("canvas controller not found (probably bacaouse of Plafrom.runLater call)! Retrying...");
        try { Thread.sleep(100); } catch (Exception e) { System.out.println("Sleep fail!"); }
        return getCanvasController(id);
    }

    private static Stage primaryStage;
    private static ArrayList<CanvasController> canvasControllers = new ArrayList<>();
    private static STTabPane stTabPane;


}
