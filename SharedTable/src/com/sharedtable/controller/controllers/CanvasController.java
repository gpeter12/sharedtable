package com.sharedtable.controller.controllers;

import com.sharedtable.controller.*;
import com.sharedtable.controller.commands.ChangeStateCommand;
import com.sharedtable.controller.commands.ClearCommand;
import com.sharedtable.controller.commands.DrawLineCommand;
import com.sharedtable.model.NetworkService;
import com.sharedtable.view.MainCanvas;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class CanvasController {

    public CanvasController(MainCanvas mainCanvas) {
        this.mainCanvas = mainCanvas;
        this.stateCaretaker = new StateCaretaker();
        this.stateOriginator = new StateOriginator();

        StateMemento firstMemento = stateOriginator.createMemento();
        //Az első mementónak egyezményesen MINDÍG ez a címe, hogy vissza lehessen rá vonni
        //d38cc911-caf6-4541-b58f-1c5b7c817e05
        firstMemento.setId(UUID.fromString("d38cc911-caf6-4541-b58f-1c5b7c817e05"));
        actMementoID = firstMemento.getId();
        System.out.println("FIRST MEMENTO "+actMementoID);
        stateCaretaker.addMemento(firstMemento,true);
        commandExecuterThread.start();
    }

    public void mouseDown(Point p) {
        lastPoint = p;
        isMouseDown = true;
        NetworkService.sendMementoOpenerSignal(UserID.getUserID(),stateOriginator.getNextMementoID(),true);
    }

    public void mouseUp(Point p) {
        isMouseDown = false;
        lastPoint = p;
        NetworkService.sendMementoCloserSignal(UserID.getUserID(),insertNewMementoAfterActual(true).getId(),true);
    }

    public void mouseMove(Point p) {
        if (isMouseDown) {
            Command command;
            if (currentMode == DrawingMode.ContinousLine) {
                command = new DrawLineCommand(mainCanvas, lastPoint, p, UserID.getUserID());
            } else {
                throw new RuntimeException("no drawing mode selected");
            }
            stateOriginator.addCommand(command);
            commandExecuterThread.addCommandToCommandQueue(command);
            lastPoint = p;
            NetworkService.propagateCommandDownwards(command);
            NetworkService.propagateCommandUpwards(command);
        }
    }

    public void clearCanvas() {
        Command command = new ClearCommand(this, UserID.getUserID(), stateOriginator.getNextMementoID());
        commandExecuterThread.addCommandToCommandQueue(command);
        //nem láncoljuk az új mementót ilyenkor, mert a visszavonás visszahozná az előtte levő állapotokat
        insertNewMementoAfterActual(false);
        NetworkService.propagateCommandUpwards(command);
        NetworkService.propagateCommandDownwards(command);
    }

    public void processSateChangeCommand(UUID targetMementoID) {
        restoreMemento(stateCaretaker.getMementoByID(targetMementoID));
    }

    public void processRemoteCommand(Command receivedCommand) {
        commandExecuterThread.addCommandToCommandQueue(receivedCommand);
    }

    public void stop() {
        commandExecuterThread.timeToStop();
    }

    public void redo() {
        restoreNextMemento();
    }

    public void undo() {
        restorePreviosMemento();
    }


    public StateCaretaker getStateCaretaker() {
        return stateCaretaker;
    }

    public StateOriginator getStateOriginator() {
        return stateOriginator;
    }

    public MainCanvas getMainCanvas() {
        return mainCanvas;
    }

    public StateMemento insertRemoteMementoAfterActual(UUID id,ArrayList<Command> commands, boolean link,UUID creatorID) {
        //mi történik helyileg, ha valaki távol befejez egy rajzolást a rajzolásom alatt
        if(!stateOriginator.isCommandBufferEmpty()) {//ha nem üres akkor épp rajzolok...
            NetworkService.sendMementoCloserSignal(UserID.getUserID(),insertNewMementoAfterActual(true).getId(),true);
        }
        StateMemento memento = insertNewMementoAfterActual(link);
        memento.setId(id);
        memento.setCreatorID(creatorID);
        memento.addCommands(commands);
        actMementoID = memento.getId();
        return memento;
    }

    public ArrayList<StateMemento> getMementos() {return stateCaretaker.getMementos();}

    public void printAllMementos() {
        stateCaretaker.printAllMementos();
    }

    public UUID getCurrentMementoID() {return actMementoID;}

    //////////////////////////////////////private section////////////////////////////////////////

    private StateMemento insertNewMementoAfterActual(boolean link) {//link: kell-e láncolni az új mementót a régivel
        try { semaphore.acquire(); } catch (Exception e) {System.out.println(e);}
        StateMemento memento = stateOriginator.createMemento();
        //amin éppen vagy az az utolsó-e
        if (actMementoID.equals(stateCaretaker.getLastMementoID())) {
            stateCaretaker.addMemento(memento, link);
        } else {
            stateCaretaker.addMemento(memento, actMementoID, link);
        }
        actMementoID = memento.getId();
        semaphore.release();
        return memento;
    }

    private void restorePreviosMemento() {
        System.out.println(stateCaretaker.getMementoIndexByID(actMementoID) - 1);
        StateMemento memento =stateCaretaker.getMementoByIndex(
                stateCaretaker.getMementoIndexByID(
                        actMementoID) - 1);
        createAndSendChangeStateCommand(memento.getId());
        restoreMemento(memento);
    }

    private void restoreNextMemento() {
        System.out.println(stateCaretaker.getMementoIndexByID(actMementoID) + 1);
        StateMemento memento = stateCaretaker.getMementoByIndex(
                stateCaretaker.getMementoIndexByID(
                        actMementoID) + 1);
        createAndSendChangeStateCommand(memento.getId());
        restoreMemento(memento);

    }

    private void createAndSendChangeStateCommand(UUID mementoID) {
        Command changeStateCommand = new ChangeStateCommand(this,UserID.getUserID(),mementoID);
        NetworkService.propagateCommandDownwards(changeStateCommand);
        NetworkService.propagateCommandUpwards(changeStateCommand);
    }

    private void restoreMemento(StateMemento memento) {
        commandExecuterThread.addCommandToCommandQueue(new ClearCommand(this, UserID.getUserID(),
                stateOriginator.getNextMementoID()));
        actMementoID = memento.getId();
        for (Command act : memento.getAllCommandsWPrev()) {
            commandExecuterThread.addCommandToCommandQueue(act);
        }
    }


    private StateCaretaker stateCaretaker;
    private StateOriginator stateOriginator;
    private boolean isMouseDown = false;
    private Point lastPoint;
    private MainCanvas mainCanvas;
    private DrawingMode currentMode = DrawingMode.ContinousLine;
    private UUID actMementoID;
    private CommandExecuterThread commandExecuterThread = new CommandExecuterThread();
    private Semaphore semaphore = new Semaphore(1);

}
