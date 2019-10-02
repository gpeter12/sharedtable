package controller;

import view.MainCanvas;

import java.util.UUID;

public class CanvasController {

    public CanvasController(MainCanvas mainCanvas) {
        this.mainCanvas = mainCanvas;
        this.stateCaretaker = new StateCaretaker();
        this.stateOriginator = new StateOriginator();
        StateMemento firstMemento = stateOriginator.createMemento();
        actMementoID = firstMemento.getId();
        System.out.println("First Memento ID " + actMementoID);
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

    public void mouseMove(Point p) {
        if(isMouseDown) {
            Command command = null;
            if(currentMode == DrawingMode.ContinousLine){
                command = new DrawLineCommand(mainCanvas,lastPoint,p);
            }
            stateOriginator.addCommand(command);
            command.execute();
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
        mainCanvas.clear();
        actMementoID = memento.getId();
        for (Command act: memento.getAllCommands()) {
            act.execute();
        }
    }

    StateCaretaker stateCaretaker;
    StateOriginator stateOriginator;
    boolean isMouseDown = false;
    Point lastPoint;
    MainCanvas mainCanvas;
    DrawingMode currentMode = DrawingMode.ContinousLine;
    UUID actMementoID;
}
