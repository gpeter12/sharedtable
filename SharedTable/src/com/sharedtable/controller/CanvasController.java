package com.sharedtable.controller;

import com.sharedtable.LoggerConfig;
import com.sharedtable.controller.commands.*;
import com.sharedtable.model.Network.NetworkService;
import com.sharedtable.view.MainView;
import com.sharedtable.view.STCanvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

public class CanvasController {

    public CanvasController(STCanvas STCanvas, UUID canvasID) {
        logger = Logger.getLogger(MainView.class.getName());
        this.STCanvas = STCanvas;
        this.canvasID = canvasID;
        STCanvas.initEventHandlers(this);
        this.stateCaretaker = new StateCaretaker();
        this.stateOriginator = new StateOriginator();

        StateMemento firstMemento = stateOriginator.createMemento();
        firstMemento.setId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        actMementoID = firstMemento.getId();
        stateCaretaker.addMemento(firstMemento,true);
        commandExecutorThread.start();
    }

    public synchronized void mouseDown(Point p) {
        try {semaphore.acquire(); } catch (InterruptedException e) {e.printStackTrace(); }
        lastPoint = p;
        isMouseDown = true;
        NetworkService.sendMementoOpenerSignal(UserID.getUserID(),canvasID,stateOriginator.getNextMementoID(),true);
    }

    public synchronized void mouseUp(Point p) {
        isMouseDown = false;
        if(currentMode == DrawingMode.ContinousLine) {
            lastPoint = p;

        } else if(currentMode == DrawingMode.Rectangle) {
            currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
            Command command = new DrawRectangleCommand(this,UserID.getUserID(),currentRect,currentColor, currentLineWidth);
            stateOriginator.addCommand(command);
            commandExecutorThread.addCommandToCommandQueue(command);
            NetworkService.propagateCommandDownwards(command);
            NetworkService.propagateCommandUpwards(command);
        } else if(currentMode == DrawingMode.Ellipse) {
            currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
            Command command = new DrawEllipseCommand(this,UserID.getUserID(),currentRect,currentColor, currentLineWidth);
            stateOriginator.addCommand(command);
            commandExecutorThread.addCommandToCommandQueue(command);
            NetworkService.propagateCommandDownwards(command);
            NetworkService.propagateCommandUpwards(command);
        } else if(currentMode == DrawingMode.Triangle) {
            currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
            Command command = new DrawTriangleCommand(this,UserID.getUserID(),currentRect,currentColor, currentLineWidth);
            stateOriginator.addCommand(command);
            commandExecutorThread.addCommandToCommandQueue(command);
            NetworkService.propagateCommandDownwards(command);
            NetworkService.propagateCommandUpwards(command);
        } else if(currentMode == DrawingMode.Image) {
            currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(), lastPoint.getY(), p.getX() - lastPoint.getX(), p.getY() - lastPoint.getY()));
            DrawImageCommand drawImageCommand = new DrawImageCommand(this, UserID.getUserID(), currentRect,currentImage,stateOriginator.getNextMementoID());
            drawImageCommand.setImageSize(drawImageCommand.getImageBytes().length);
            //drawImageCommand.printByteArray(drawImageCommand.getImageBytes());

            Command command = drawImageCommand;
            stateOriginator.addCommand(command);
            commandExecutorThread.addCommandToCommandQueue(command);
            NetworkService.forwardDrawImageCommandDownwards(drawImageCommand);
            NetworkService.forwardDrawImageCommandUpwards(drawImageCommand);
        }
        NetworkService.sendMementoCloserSignal(UserID.getUserID(),canvasID,insertNewMementoAfterActual(true).getId(),true);
        semaphore.release();
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
            } else if(currentMode == DrawingMode.Triangle) {
                currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
                command = new DrawTriangleCommand(this,UserID.getUserID(),currentRect,currentColor, currentLineWidth);
                processSateChangeCommand(getCurrentMementoID());
                commandExecutorThread.addCommandToCommandQueue(command);
            } else if(currentMode == DrawingMode.Ellipse) {
                currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
                command = new DrawEllipseCommand(this,UserID.getUserID(),currentRect,currentColor, currentLineWidth);
                processSateChangeCommand(getCurrentMementoID());
                commandExecutorThread.addCommandToCommandQueue(command);
            } else if(currentMode == DrawingMode.Image) {
                currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
                command = new DrawImageCommand(this,UserID.getUserID(),currentRect,currentImage,stateOriginator.getNextMementoID());
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
        if(TabController.getActualCanvasControler().equals(this)){
            restoreNextMemento();
            Sleep.sleep(100,logger);
        }

    }

    public void undo() {
        if(TabController.getActualCanvasControler().equals(this)){
            restorePreviosMemento();
            Sleep.sleep(100,logger);
        }

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
            logger.info("remote memento dropped: "+id.toString());
            return;
        }
        logger.info("inserting remote memento (ID:"+id.toString()+" from "+creatorID);
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

    public void drawEllipse(Rectangle rectangle, Color color, int lineWidth) {
        STCanvas.setColor(color);
        STCanvas.setLineWidth(lineWidth);
        STCanvas.drawEllipse(rectangle);
    }

    public void drawTriangle(Rectangle rectangle, Color color, int lineWidth) {
        STCanvas.setColor(color);
        STCanvas.setLineWidth(lineWidth);
        Point upperMidPoint = calculateLineMindPoint(new Point(rectangle.getX(),rectangle.getY()),
                new Point(rectangle.getX()+rectangle.getWidth(),rectangle.getY()));
        STCanvas.drawTriangle(new Point(rectangle.getX(),rectangle.getY()+rectangle.getHeight()),upperMidPoint,
                new Point(rectangle.getX()+rectangle.getWidth(),rectangle.getY()+rectangle.getHeight()));
    }

    public void drawImage(Image image, Rectangle rectangle) {
        STCanvas.drawImage(image,rectangle);
    }

    public void onWidthChanged() {
        processSateChangeCommand(actMementoID);
    }

    public void onHeightChanged() {
        processSateChangeCommand(actMementoID);

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

    public void setCurrentImage(Image image) {
        this.currentImage = image;
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
        return memento;
    }

    private Point calculateLineMindPoint(Point a, Point b) {
        return new Point(0.5*(a.getX()+b.getX()),a.getY());
    }

    private void restorePreviosMemento() {
        logger.info("restoring memento #"+String.valueOf(stateCaretaker.getMementoIndexByID(actMementoID) - 1));
        StateMemento memento =stateCaretaker.getMementoByIndex(
                stateCaretaker.getMementoIndexByID(
                        actMementoID) - 1);
        createAndSendChangeStateCommand(memento.getId());
        restoreMemento(memento);
    }

    private void restoreNextMemento() {
        logger.info("restoring memento #"+String.valueOf(stateCaretaker.getMementoIndexByID(actMementoID) + 1));
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
    private Image currentImage;
    private STCanvas STCanvas;
    private DrawingMode currentMode = DrawingMode.ContinousLine;
    private UUID actMementoID;
    private CommandExecutorThread commandExecutorThread = new CommandExecutorThread();
    private Color currentColor = Color.BLACK;
    private int currentLineWidth = 1;
    private Semaphore semaphore = new Semaphore(1);
    private Logger logger = null;


}
