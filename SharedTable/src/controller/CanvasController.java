package controller;

import controller.commands.ClearCommand;
import controller.commands.DrawLineCommand;
import model.NetworkService;
import view.MainCanvas;

import java.util.UUID;

public class CanvasController {

    public CanvasController(MainCanvas mainCanvas) {
        this.mainCanvas = mainCanvas;
        this.stateCaretaker = new StateCaretaker();
        this.stateOriginator = new StateOriginator();

        StateMemento firstMemento = stateOriginator.createMemento();
        actMementoID = firstMemento.getId();
        stateCaretaker.addMemento(firstMemento);
        commandExecuterThread.start();
    }



    public void mouseDown(Point p) {
        lastPoint = p;
        isMouseDown = true;
    }

    public void mouseUp(Point p) {
        isMouseDown = false;
        lastPoint = p;
        insertNewMementoAfterActual();
    }



    public void mouseMove(Point p) {
        if(isMouseDown) {
            Command command = null;
            if(currentMode == DrawingMode.ContinousLine){
                command = new DrawLineCommand(mainCanvas,lastPoint,p);
            } else {
                throw new RuntimeException("no drawing mode selected");
            }
            stateOriginator.addCommand(command);
            commandExecuterThread.addCommandToCommandQueue(command);
            lastPoint = p;
            NetworkService.propagateDataDownwards(command);
            NetworkService.propagateDataUpwards(command);
        }
    }

    private UUID insertNewMementoAfterActual() {
        StateMemento memento = stateOriginator.createMemento();
        if(actMementoID.equals(stateCaretaker.getLastMementoID())) {
            stateCaretaker.addMemento(memento);
        }
        else {
            stateCaretaker.addMemento(memento,actMementoID);
        }

        actMementoID = memento.getId();
        System.out.println("actMementoID "+actMementoID);
        return memento.getId();
    }

    public void restorePreviosMemento() {
        System.out.println(stateCaretaker.getMementoIndexByID(actMementoID)-1);
        restoreMemento(
                stateCaretaker.getMementoByIndex(
                        stateCaretaker.getMementoIndexByID(
                                actMementoID)-1));
    }

    public void restoreNextMemento() {
        System.out.println(stateCaretaker.getMementoIndexByID(actMementoID)+1);
        restoreMemento(
                stateCaretaker.getMementoByIndex(
                        stateCaretaker.getMementoIndexByID(
                                actMementoID)+1));

    }

    private void restoreMemento(StateMemento memento) {
        commandExecuterThread.addCommandToCommandQueue(new ClearCommand(mainCanvas));
        actMementoID = memento.getId();
        for (Command act: memento.getAllCommands()) {
            commandExecuterThread.addCommandToCommandQueue(act);
        }
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

    StateCaretaker stateCaretaker;
    StateOriginator stateOriginator;
    boolean isMouseDown = false;
    Point lastPoint;
    MainCanvas mainCanvas;
    DrawingMode currentMode = DrawingMode.ContinousLine;
    UUID actMementoID;
    CommandExecuterThread commandExecuterThread = new CommandExecuterThread();



}
