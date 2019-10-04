package controller;

import view.MainCanvas;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class CanvasController {

    public CanvasController(MainCanvas mainCanvas) {
        this.mainCanvas = mainCanvas;
        this.stateCaretaker = new StateCaretaker();
        this.stateOriginator = new StateOriginator();


        StateMemento firstMemento = stateOriginator.createMemento();
        actMementoID = firstMemento.getId();
        stateCaretaker.addMemento(firstMemento);
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

    private void addCommandToCommandQueue(Command command) {
        try {
            commandQueue.put(command);
        } catch (Exception e) {
            addCommandToCommandQueue(command);
        }
    }

    public void mouseMove(Point p) {
        if(isMouseDown) {
            Command command = null;
            if(currentMode == DrawingMode.ContinousLine){
                command = new DrawLineCommand(mainCanvas,lastPoint,p);
            }
            stateOriginator.addCommand(command);
            addCommandToCommandQueue(command);
            lastPoint = p;
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
        addCommandToCommandQueue(new ClearCommand(mainCanvas));
        actMementoID = memento.getId();
        for (Command act: memento.getAllCommands()) {
            addCommandToCommandQueue(act);
        }
    }

    private void commandExecuter() throws InterruptedException {
        for (;;) {
            commandQueue.take().execute();
        }
    }

    StateCaretaker stateCaretaker;
    StateOriginator stateOriginator;

    boolean isMouseDown = false;
    Point lastPoint;
    MainCanvas mainCanvas;
    DrawingMode currentMode = DrawingMode.ContinousLine;
    UUID actMementoID;
    BlockingQueue<Command> commandQueue;
}
