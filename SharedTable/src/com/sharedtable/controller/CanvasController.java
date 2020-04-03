package com.sharedtable.controller;

import com.sharedtable.controller.commands.*;
import com.sharedtable.model.NetworkService;
import com.sharedtable.view.STCanvas;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class CanvasController {

    public CanvasController(STCanvas STCanvas, UUID canvasID) {
        this.STCanvas = STCanvas;
        this.canvasID = canvasID;
        STCanvas.initEventHandlers(this);
        this.stateCaretaker = new StateCaretaker();
        this.stateOriginator = new StateOriginator();

        StateMemento firstMemento = stateOriginator.createMemento();
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
        if(currentMode == DrawingMode.ContinousLine) {
            lastPoint = p;
            NetworkService.sendMementoCloserSignal(UserID.getUserID(),canvasID,insertNewMementoAfterActual(true).getId(),true);
        } else if(currentMode == DrawingMode.Rectangle) {
            currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
            Command command = new DrawRectangleCommand(this,UserID.getUserID(),currentRect,currentColor, currentLineWidth);
            stateOriginator.addCommand(command);
            commandExecutorThread.addCommandToCommandQueue(command);
            //lastPoint = p;
            NetworkService.propagateCommandDownwards(command);
            NetworkService.propagateCommandUpwards(command);
            NetworkService.sendMementoCloserSignal(UserID.getUserID(),canvasID,insertNewMementoAfterActual(true).getId(),true);
        }
    }

    public void mouseMove(Point p) {
        if (isMouseDown) {
            Command command;
            if (currentMode == DrawingMode.ContinousLine) {
                command = new DrawLineCommand(this, lastPoint, p, UserID.getUserID(),currentColor,currentLineWidth);
                stateOriginator.addCommand(command);
                commandExecutorThread.addCommandToCommandQueue(command);
                lastPoint = p;
                NetworkService.propagateCommandDownwards(command);
                NetworkService.propagateCommandUpwards(command);
            } else if(currentMode == DrawingMode.Rectangle) {
                currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
                command = new DrawRectangleCommand(this,UserID.getUserID(),currentRect,currentColor, currentLineWidth);
                processSateChangeCommand(getCurrentMementoID());
                commandExecutorThread.addCommandToCommandQueue(command);
            } else if(currentMode == DrawingMode.Rectangle) {
                currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
                command = new DrawRectangleCommand(this,UserID.getUserID(),currentRect,currentColor, currentLineWidth);
                processSateChangeCommand(getCurrentMementoID());
                commandExecutorThread.addCommandToCommandQueue(command);
            }
            else {
                throw new RuntimeException("no drawing mode selected");
            }
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
        Sleep.sleep(100);
        if(TabController.getActualCanvasControler().equals(this))
            restoreNextMemento();
    }

    public void undo() {
        Sleep.sleep(100);
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

    public void insertRemoteMementoAfterActual(UUID id, ArrayList<Command> commands, boolean link, UUID creatorID) {
        if(stateCaretaker.hasMememnto(id)){
            System.out.println("--remote memento dropped: "+id.toString());
            return;
        }


        //mi történik helyileg, ha valaki távol befejez egy rajzolást a rajzolásom alatt
        if(!stateOriginator.isCommandBufferEmpty()) {//ha nem üres akkor épp rajzolok...
            NetworkService.sendMementoCloserSignal(UserID.getUserID(),canvasID,insertNewMementoAfterActual(true).getId(),true);
        }
        StateMemento memento = insertNewMementoAfterActual(link);
        memento.setId(id);
        memento.setCreatorID(creatorID);
        memento.addCommands(commands);
        actMementoID = memento.getId();
    }

    public ArrayList<StateMemento> getMementos() {return stateCaretaker.getMementos();}

    public void printAllMementos() {
        stateCaretaker.printAllMementos();
    }

    public UUID getCurrentMementoID() {return actMementoID;}

    public UUID getCanvasID() {return canvasID;}

    public RemoteDrawLineCommandBufferHandler getRemoteDrawLineCommandBufferHandler()
    {return remoteDrawLineCommandBufferHandler;}


    public void drawLine(Point x, Point y, Color color, int lineWidth) {
        STCanvas.setColor(color);
        STCanvas.setLineWidth(lineWidth);
        STCanvas.drawLine(x,y);
    }

    public void drawRectangle(Rectangle rectangle, Color color, int lineWidth) {
        STCanvas.setColor(color);
        STCanvas.setLineWidth(lineWidth);
        STCanvas.drawRectangle(rectangle);
    }

    public void setColor(Color color) {
        currentColor = color;
        STCanvas.setColor(color);
    }

    public void setLineWidth(int lineWidth) {
        currentLineWidth = lineWidth;
        STCanvas.setLineWidth(lineWidth);
    }

    public void setDrawingMode(DrawingMode mode) {
        currentMode = mode;
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

    private synchronized StateMemento insertNewMementoAfterActual(boolean link) {//link: kell-e láncolni az új mementót a régivel (clear command után nem szabad)
        StateMemento memento = stateOriginator.createMemento();
        //amin éppen vagy az az utolsó-e
        if (actMementoID.equals(stateCaretaker.getLastMementoID())) {
            stateCaretaker.addMemento(memento, link);
        } else {
            stateCaretaker.addMemento(memento, actMementoID, link);
        }
        actMementoID = memento.getId();
        System.out.println("new memento added: "+actMementoID);
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

    private Rectangle fixRectangleNegativeWidthHeight(Rectangle input) {
        if(input.getWidth() >= 0 && input.getHeight() >= 0)
            return input;
        if(input.getWidth() < 0 && input.getHeight() < 0) {
            return new Rectangle(input.getX() - input.getWidth()*-1,input.getY() - input.getHeight()*-1,input.getWidth()*-1,(input.getHeight()*-1));
        } if(input.getHeight() < 0) {
            return new Rectangle(input.getX(),input.getY() + input.getHeight(),input.getWidth(),(input.getHeight()*-1));
        } if(input.getWidth() < 0) {
            return new Rectangle(input.getX() + input.getWidth(),input.getY(),input.getWidth()*-1,input.getHeight());
        }
        throw new UnsupportedOperationException();
    }

    private RemoteDrawLineCommandBufferHandler remoteDrawLineCommandBufferHandler =
            new RemoteDrawLineCommandBufferHandler(this);
    private UUID canvasID;
    private StateCaretaker stateCaretaker;
    private StateOriginator stateOriginator;
    private boolean isMouseDown = false;
    private Point lastPoint;
    private Rectangle currentRect;
    private STCanvas STCanvas;
    private DrawingMode currentMode = DrawingMode.Rectangle;
    private UUID actMementoID;
    private CommandExecutorThread commandExecutorThread = new CommandExecutorThread();
    private Color currentColor = Color.BLACK;
    private int currentLineWidth = 1;



}
