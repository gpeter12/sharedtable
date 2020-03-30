package com.sharedtable.controller.controllers;

import com.sharedtable.controller.*;
import com.sharedtable.controller.commands.ChangeStateCommand;
import com.sharedtable.controller.commands.ClearCommand;
import com.sharedtable.controller.commands.Command;
import com.sharedtable.controller.commands.DrawLineCommand;
import com.sharedtable.model.NetworkService;
import com.sharedtable.view.STCanvas;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class CanvasController {

    public CanvasController(STCanvas STCanvas, UUID canvasID) {
        this.STCanvas = STCanvas;
        this.canvasID = canvasID;
        STCanvas.initEventHandlers(this);
        this.stateCaretaker = new StateCaretaker();
        this.stateOriginator = new StateOriginator();


        StateMemento firstMemento = stateOriginator.createMemento();
        //Az első mementónak egyezményesen MINDÍG ez a címe, hogy vissza lehessen rá vonni
        //d38cc911-caf6-4541-b58f-1c5b7c817e05
        firstMemento.setId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        actMementoID = firstMemento.getId();
        System.out.println("FIRST MEMENTO "+actMementoID);
        stateCaretaker.addMemento(firstMemento,true);
        commandExecutorThread.start();
    }

    public void mouseDown(Point p) {
        lastPoint = p;
        isMouseDown = true;
        NetworkService.sendMementoOpenerSignal(UserID.getUserID(),canvasID,stateOriginator.getNextMementoID(),true);
    }

    public void mouseUp(Point p) {
        isMouseDown = false;
        lastPoint = p;
        NetworkService.sendMementoCloserSignal(UserID.getUserID(),canvasID,insertNewMementoAfterActual(true).getId(),true);
    }

    public void mouseMove(Point p) {
        if (isMouseDown) {
            Command command;
            if (currentMode == DrawingMode.ContinousLine) {
                command = new DrawLineCommand(this, lastPoint, p, UserID.getUserID());
            } else {
                throw new RuntimeException("no drawing mode selected");
            }
            stateOriginator.addCommand(command);
            commandExecutorThread.addCommandToCommandQueue(command);
            lastPoint = p;
            NetworkService.propagateCommandDownwards(command);
            NetworkService.propagateCommandUpwards(command);
        }
    }

    public void clearCanvas() {
        Command command = new ClearCommand(this, UserID.getUserID(), stateOriginator.getNextMementoID());
        commandExecutorThread.addCommandToCommandQueue(command);
        //nem láncoljuk az új mementót ilyenkor, mert a visszavonás visszahozná az előtte levő állapotokat
        insertNewMementoAfterActual(false);
        NetworkService.propagateCommandUpwards(command);
        NetworkService.propagateCommandDownwards(command);
    }

    public void processSateChangeCommand(UUID targetMementoID) {
        restoreMemento(stateCaretaker.getMementoByID(targetMementoID));
    }

    public void processRemoteCommand(Command receivedCommand) {
        commandExecutorThread.addCommandToCommandQueue(receivedCommand);
    }

    public void stop() {
        commandExecutorThread.timeToStop();
    }

    public void redo() {
        if(TabController.getActualCanvasControler().equals(this))
            restoreNextMemento();
    }

    public void undo() {
        if(TabController.getActualCanvasControler().equals(this))
            restorePreviosMemento();
    }


    public StateCaretaker getStateCaretaker() {
        return stateCaretaker;
    }

    public StateOriginator getStateOriginator() {
        return stateOriginator;
    }

    public STCanvas getSTCanvas() {
        return STCanvas;
    }

    public StateMemento insertRemoteMementoAfterActual(UUID id, ArrayList<Command> commands, boolean link, UUID creatorID) {
        //mi történik helyileg, ha valaki távol befejez egy rajzolást a rajzolásom alatt
        if(!stateOriginator.isCommandBufferEmpty()) {//ha nem üres akkor épp rajzolok...
            NetworkService.sendMementoCloserSignal(UserID.getUserID(),canvasID,insertNewMementoAfterActual(true).getId(),true);
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

    public UUID getCanvasID() {return canvasID;}

    public RemoteDrawLineCommandBufferHandler getRemoteDrawLineCommandBufferHandler()
    {return remoteDrawLineCommandBufferHandler;}


    public void drawLine(Point x, Point y) {
        STCanvas.drawLine(x,y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CanvasController that = (CanvasController) o;
        return canvasID.equals(that.canvasID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(canvasID);
    }
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
        commandExecutorThread.addCommandToCommandQueue(new ClearCommand(this, UserID.getUserID(),
                stateOriginator.getNextMementoID()));
        actMementoID = memento.getId();
        for (Command act : memento.getAllCommandsWPrev()) {
            commandExecutorThread.addCommandToCommandQueue(act);
        }
    }

    private RemoteDrawLineCommandBufferHandler remoteDrawLineCommandBufferHandler =
            new RemoteDrawLineCommandBufferHandler(this);
    private UUID canvasID;
    private StateCaretaker stateCaretaker;
    private StateOriginator stateOriginator;
    private boolean isMouseDown = false;
    private Point lastPoint;
    private STCanvas STCanvas;
    private DrawingMode currentMode = DrawingMode.ContinousLine;
    private UUID actMementoID;
    private CommandExecutorThread commandExecutorThread = new CommandExecutorThread();
    private Semaphore semaphore = new Semaphore(1);


}
