package com.sharedtable.controller;

import com.sharedtable.model.network.NetworkService;
import com.sharedtable.model.network.signals.CloseTabSignal;
import com.sharedtable.model.network.signals.NewTabSignal;
import com.sharedtable.view.STTab;
import com.sharedtable.view.STTabPane;
import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

public class TabController {

    private Stage primaryStage;
    private ArrayList<CanvasController> canvasControllers = new ArrayList<>();
    private STTabPane stTabPane;
    private Logger logger = Logger.getLogger(MainViewController.class.getName());
    private static TabController instance = new TabController();
    private static Semaphore semaphore = new Semaphore(1);

    private TabController() {

    }

    public static TabController getInstance() {
        if(instance == null) {
            instance = new TabController();
        }
        return instance;
    }

    public void init(STTabPane stTabPane, Stage primaryStage) {
        logger = Logger.getLogger(MainViewController.class.getName());
        this.primaryStage = primaryStage;
        this.stTabPane = stTabPane;
        createNewTab(UUID.randomUUID(),UserID.getInstance().getNickname()+" hozott vászna");
        stTabPane.setClosable(false);
    }

    public UUID copyTabWithMementos(UUID canvasID, String tabName) {

        ArrayList<StateMemento> currentMementos = getCanvasController(canvasID).getMementos();
        ArrayList<StateMemento> deepCopy = new ArrayList<>();

        for(StateMemento act : currentMementos) {
            deepCopy.add(new StateMemento(act,logger));
        }

        UUID newTabId = UUID.randomUUID();
        createNewTab(newTabId,tabName);
        NetworkService.getInstance().sendNewTabSignal(UserID.getInstance().getUserID(),newTabId, tabName);
        getCanvasController(newTabId).loadMementos(deepCopy);
        return newTabId;
    }

    public void createNewTab(UUID canvasID, String tabName) {
        logger.info("creating new canvas: "+tabName+" ID: "+canvasID);
        stTabPane.createNewTab(canvasID,tabName);
        CanvasController canvasController = new CanvasController(stTabPane.getCanvas(canvasID), canvasID);
        initKeyboardEventHandler(primaryStage,canvasController);
        canvasControllers.add(canvasController);
        stTabPane.setClosable(true);
    }

    public boolean hasCanvas(UUID id) {
        for(CanvasController act : canvasControllers) {
            if(act.getCanvasID().equals(id))
                return true;
        }
        return false;
    }

    public ArrayList<CanvasController> getAllCanvasControllers() {
        return canvasControllers;
    }

    public void initKeyboardEventHandler(Stage primaryStage, CanvasController canvasController){
        KeyboardEventHandler keyboardEventHandler = new KeyboardEventHandler(canvasController);
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED,
                event -> {
                    keyboardEventHandler.handleEvent(event);
                });
    }

    public void onTabClosed(UUID tabID) {
        System.out.println(getAllCanvasControllers().size());
        if(getAllCanvasControllers().size()>1){
            removeTab(tabID);
            NetworkService.getInstance().sendCloseTabSignal(UserID.getInstance().getUserID(),tabID);
        }
    }

    private void removeTab(UUID tabId) {
        getCanvasController(tabId).stop();
        removeCanvasController(tabId);
        if(getAllCanvasControllers().size()<2){
            stTabPane.setClosable(false);
        }
    }

    public void stop() {
        for(CanvasController act : canvasControllers) {
            act.stop();
        }
    }

    public CanvasController getActualCanvasController() {
        return getCanvasController(stTabPane.getSelectedTabID());
    }

    public int getMementoCountOnAllCanvasController() {
        int sum=0;
        for(CanvasController act : canvasControllers) {
            sum += act.getMementos().size();
        }
        return sum;
    }

    public ArrayList<NewTabSignal> generateNewTabSignalsFromAllTab() {
        ArrayList<NewTabSignal> signals = new ArrayList<>();
        for(STTab act : stTabPane.getAllTabs()) {
            signals.add(new NewTabSignal(UserID.getInstance().getUserID(),
                    getCanvasController(UUID.fromString(act.getId())).getCanvasID(),
                    act.getText()));
        }
        return signals;
    }

    public void handleNewTabSingal(NewTabSignal newTabSignal) {
        try { semaphore.acquire(); } catch (InterruptedException e) { e.printStackTrace(); }
        System.out.println("acquired in handleNewTabSingal");
        Platform.runLater(() -> {
            if(hasCanvas(newTabSignal.getCanvasID())) {
                semaphore.release();
                return;
            }
            createNewTab(newTabSignal.getCanvasID(),newTabSignal.getTabName());
            System.out.println("released in handleNewTabSingal");
            semaphore.release();
        });
    }

    public void handleCloseTabSignal(CloseTabSignal closeTabSignal) {
        Platform.runLater(() -> {
            removeTab(closeTabSignal.getCanvasID());
            stTabPane.removeTab(closeTabSignal.getCanvasID());
                });
    }

    public CanvasController getCanvasController(UUID id) {
        try { semaphore.acquire(); } catch (InterruptedException e) { e.printStackTrace(); }
        System.out.println("acquired in getCanvasController");
        for(CanvasController act : canvasControllers) {
            if(act.getCanvasID().equals(id)){
                semaphore.release();
                System.out.println("released in getCanvasController");
                return act;
            }
        }
        //logger.warning("canvas controller not found (probably because of Platform.runLater call)! Retrying...");
        //Sleep.sleep(100,logger);
        throw new NotFoundException("CanvasController not found. ID: "+id);
    }

    public void renameTab(UUID canvasID, String tabName) {
        stTabPane.renameTab(canvasID,tabName);
    }


    private void removeCanvasController(UUID id) {
        canvasControllers.remove(getCanvasController(id));
    }

}
