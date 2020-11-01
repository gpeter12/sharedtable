package com.sharedtable.controller;

import com.sharedtable.Constants;
import com.sharedtable.controller.commands.*;
import com.sharedtable.model.network.NetworkService;
import com.sharedtable.view.STCanvas;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

public class CanvasController {

    private RemoteDrawLineCommandBufferHandler remoteDrawLineCommandBufferHandler =
            new RemoteDrawLineCommandBufferHandler(this);
    private UUID canvasID;
    private StateCaretaker stateCaretaker;
    private StateOriginator stateOriginator;
    private boolean isMouseDown = false;
    private Point lastPoint;
    private Rectangle currentRect;
    private Image currentImage;
    private STCanvas sTCanvas;
    private DrawingMode currentMode = DrawingMode.ContinousLine;
    private UUID actMementoID;
    private CommandExecutorThread commandExecutorThread = new CommandExecutorThread();
    private Color currentColor = Color.BLACK;
    private int currentLineWidth = 1;
    private Semaphore semaphore = new Semaphore(1);
    private Semaphore mementoAddSemaphore = new Semaphore(1);
    private Logger logger = null;
    private WritableImage currentSnapshot;
    private long nextStateRedraw=0;

    public CanvasController(STCanvas STCanvas, UUID canvasID) {
        logger = Logger.getLogger(MainViewController.class.getName());
        this.sTCanvas = STCanvas;
        this.canvasID = canvasID;
        STCanvas.initEventHandlers(this);
        this.stateCaretaker = new StateCaretaker();
        this.stateOriginator = new StateOriginator();

        StateMemento firstMemento = stateOriginator.createMemento();
        firstMemento.setPreviousMementoID(Constants.getNilUUID());
        firstMemento.setNextMementoID(Constants.getEndChainUUID());
        firstMemento.setBackLinked(false);
        firstMemento.setId(Constants.getNilUUID());
        actMementoID = firstMemento.getId();
        stateCaretaker.addFirstMemento(firstMemento);
        commandExecutorThread.start();
    }

    public synchronized void mouseDown(Point p) {
        try {semaphore.acquire(); } catch (InterruptedException e) {e.printStackTrace(); }
        lastPoint = p;
        isMouseDown = true;
        if(currentMode == DrawingMode.Ellipse ||
                currentMode == DrawingMode.Image ||
                currentMode == DrawingMode.Rectangle ||
                currentMode == DrawingMode.Triangle)
        {
            currentSnapshot = sTCanvas.createImage();
        }
        NetworkService.getInstance().sendMementoOpenerSignal(UserID.getInstance().getUserID(),canvasID);
    }

    public void mouseMove(Point p) {
        if (isMouseDown) {
            Command command;
            if (currentMode == DrawingMode.ContinousLine) {
                command = new DrawLineCommand(this, lastPoint, p, UserID.getInstance().getUserID(),currentColor,currentLineWidth);
                stateOriginator.addCommand(command);
                commandExecutorThread.addCommandToCommandQueue(command);
                lastPoint = p;
                NetworkService.getInstance().propagateCommandDownwards(command);
                NetworkService.getInstance().propagateCommandUpwards(command);
            } else if(currentMode == DrawingMode.Rectangle) {
                currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
                command = new DrawRectangleCommand(this,UserID.getInstance().getUserID(),currentRect,currentColor, currentLineWidth);
                //processStateChangeCommand(getCurrentMementoID());
                sTCanvas.setImage(currentSnapshot);
                commandExecutorThread.addCommandToCommandQueue(command);
            } else if(currentMode == DrawingMode.Triangle) {
                currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
                command = new DrawTriangleCommand(this,UserID.getInstance().getUserID(),currentRect,currentColor, currentLineWidth);
                //processStateChangeCommand(getCurrentMementoID());
                sTCanvas.setImage(currentSnapshot);
                commandExecutorThread.addCommandToCommandQueue(command);
            } else if(currentMode == DrawingMode.Ellipse) {
                currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
                command = new DrawEllipseCommand(this,UserID.getInstance().getUserID(),currentRect,currentColor, currentLineWidth);
                //processStateChangeCommand(getCurrentMementoID());
                sTCanvas.setImage(currentSnapshot);
                commandExecutorThread.addCommandToCommandQueue(command);
            } else if(currentMode == DrawingMode.Image) {
                currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
                command = new DrawImageCommand(this,UserID.getInstance().getUserID(),currentRect,currentImage,stateOriginator.getNextMementoID());
                //processStateChangeCommand(getCurrentMementoID());
                sTCanvas.setImage(currentSnapshot);
                commandExecutorThread.addCommandToCommandQueue(command);
            }
            else {
                throw new RuntimeException("no drawing mode selected");
            }
        }
    }



    public synchronized void mouseUp(Point p) {
        if(currentMode == DrawingMode.Rectangle) {
            currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
            Command command = new DrawRectangleCommand(this,UserID.getInstance().getUserID(),currentRect,currentColor, currentLineWidth);
            stateOriginator.addCommand(command);
            commandExecutorThread.addCommandToCommandQueue(command);
            NetworkService.getInstance().propagateCommandDownwards(command);
            NetworkService.getInstance().propagateCommandUpwards(command);
        } else if(currentMode == DrawingMode.Ellipse) {
            currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
            Command command = new DrawEllipseCommand(this,UserID.getInstance().getUserID(),currentRect,currentColor, currentLineWidth);
            stateOriginator.addCommand(command);
            commandExecutorThread.addCommandToCommandQueue(command);
            NetworkService.getInstance().propagateCommandDownwards(command);
            NetworkService.getInstance().propagateCommandUpwards(command);
        } else if(currentMode == DrawingMode.Triangle) {
            currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(),lastPoint.getY(),p.getX()-lastPoint.getX(),p.getY()-lastPoint.getY()));
            Command command = new DrawTriangleCommand(this,UserID.getInstance().getUserID(),currentRect,currentColor, currentLineWidth);
            stateOriginator.addCommand(command);
            commandExecutorThread.addCommandToCommandQueue(command);
            NetworkService.getInstance().propagateCommandDownwards(command);
            NetworkService.getInstance().propagateCommandUpwards(command);
        } else if(currentMode == DrawingMode.Image) {
            currentRect = fixRectangleNegativeWidthHeight(new Rectangle(lastPoint.getX(), lastPoint.getY(), p.getX() - lastPoint.getX(), p.getY() - lastPoint.getY()));
            DrawImageCommand drawImageCommand = new DrawImageCommand(this, UserID.getInstance().getUserID(), currentRect,currentImage,stateOriginator.getNextMementoID());
            drawImageCommand.setImageSize(drawImageCommand.getImageBytes().length);

            Command command = drawImageCommand;
            stateOriginator.addCommand(command);
            commandExecutorThread.addCommandToCommandQueue(command);
            NetworkService.getInstance().forwardDrawImageCommandDownwards(drawImageCommand);
            NetworkService.getInstance().forwardDrawImageCommandUpwards(drawImageCommand);
        }

        StateMemento newMemento = insertNewMementoAfterActual(true);
        NetworkService.getInstance().sendMementoCloserSignal(UserID.getInstance().getUserID(),canvasID,
                newMemento.getId(),true,newMemento.getPreviousMementoID(),
                newMemento.getNextMementoID());
        processStateChangeCommand(getCurrentMementoID());
        semaphore.release();
    }

    public ClearCommand clearCanvas() {
        StateMemento blankMemento = insertNewMementoAfterActual(false);
        Command command = new ClearCommand(this, UserID.getInstance().getUserID(),
                blankMemento.getId(),blankMemento.getPreviousMementoID(),blankMemento.getNextMementoID());
        commandExecutorThread.addCommandToCommandQueue(command);
        //nem láncoljuk az új mementót ilyenkor, mert a visszavonás visszahozná az előtte levő állapotokat
        NetworkService.getInstance().propagateCommandUpwards(command);
        NetworkService.getInstance().propagateCommandDownwards(command);
        return (ClearCommand) command;
    }

    public void processStateChangeCommand(UUID targetMementoID) {
        try{
            restoreMemento(stateCaretaker.getMementoByID(targetMementoID));
        } catch (Exception e) {
            e.printStackTrace();
            handleStateChainInconsistencyException();
        }

    }

    public void processRemoteCommand(Command receivedCommand) {
        commandExecutorThread.addCommandToCommandQueue(receivedCommand);
    }

    public void stop() {
        commandExecutorThread.timeToStop();
    }

    public synchronized void redo() {
        if(TabController.getInstance().getActualCanvasController().equals(this)){
            restoreNextMemento();
            Sleep.sleep(100,logger);
        }

    }

    public synchronized void undo() {
        if(TabController.getInstance().getActualCanvasController().equals(this)){
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
        return sTCanvas;
    }

    public void loadMementos(ArrayList<StateMemento> mementos) {
        mementos.remove(0);
        for(StateMemento act : mementos) {
            try {
                act.setCreatorID(UserID.getInstance().getUserID());
                act.setNextMemento(null);
                act.setNextMementoID(Constants.getEndChainUUID());
                if(act.getPreviousMemento() != null) {
                    act.setPreviousMemento(stateCaretaker.getLastMemento());
                }
                act.setNewCanvasControllerForCommands(this);
                stateCaretaker.addMemento(act,act.isBackLinked());
                NetworkService.getInstance().sendMementoOpenerSignal(UserID.getInstance().getUserID(),canvasID);
                for(Command actc : act.getCommands()) {
                    actc.setCreatorID(UserID.getInstance().getUserID());
                    actc.setCanvasController(this);
                    if(actc instanceof DrawImageCommand) {
                        NetworkService.getInstance().forwardDrawImageCommandDownwards((DrawImageCommand) actc);
                        NetworkService.getInstance().forwardDrawImageCommandDownwards((DrawImageCommand) actc);
                    } else {
                        NetworkService.getInstance().propagateCommandDownwards(actc);
                        NetworkService.getInstance().propagateCommandUpwards(actc);
                    }
                }
                NetworkService.getInstance().sendMementoCloserSignal(UserID.getInstance().getUserID(),
                        canvasID,act.getId(),
                        act.isBackLinked(),act.getPreviousMementoID(),act.getNextMementoID());
            } catch (StateChainInconsistencyException e) {
                e.printStackTrace();
            }
        }
        actMementoID = stateCaretaker.getLastMementoID();
        processStateChangeCommand(actMementoID);
        
    }


    private void handleStateChainInconsistencyException() {
        logger.warning("handleStateChainInconsistencyException happening");
        TabController.getInstance().copyTabWithMementos(getCanvasID(),"restored");
        TabController.getInstance().onTabClosed(getCanvasID());


    }

    public void printAllMementoData(){
        stateCaretaker.printAllMementos();
    }

    public ArrayList<StateMemento> getMementos() {return stateCaretaker.getMementos();}

    public UUID getCurrentMementoID() {return actMementoID;}

    public UUID getCanvasID() {return canvasID;}

    public RemoteDrawLineCommandBufferHandler getRemoteDrawLineCommandBufferHandler()
    {return remoteDrawLineCommandBufferHandler;}


    public void drawLine(Point x, Point y, Color color, int lineWidth) {
        sTCanvas.setColor(color);
        sTCanvas.setLineWidth(lineWidth);
        sTCanvas.drawLine(x,y);
    }

    public void drawRectangle(Rectangle rectangle, Color color, int lineWidth) {
        sTCanvas.setColor(color);
        sTCanvas.setLineWidth(lineWidth);
        sTCanvas.drawRectangle(rectangle);
    }

    public void drawEllipse(Rectangle rectangle, Color color, int lineWidth) {
        sTCanvas.setColor(color);
        sTCanvas.setLineWidth(lineWidth);
        sTCanvas.drawEllipse(rectangle);
    }

    public void drawTriangle(Rectangle rectangle, Color color, int lineWidth) {
        sTCanvas.setColor(color);
        sTCanvas.setLineWidth(lineWidth);
        Point upperMidPoint = calculateLineMindPoint(new Point(rectangle.getX(),rectangle.getY()),
                new Point(rectangle.getX()+rectangle.getWidth(),rectangle.getY()));
        sTCanvas.drawTriangle(new Point(rectangle.getX(),rectangle.getY()+rectangle.getHeight()),upperMidPoint,
                new Point(rectangle.getX()+rectangle.getWidth(),rectangle.getY()+rectangle.getHeight()));
    }

    public void drawImage(Image image, Rectangle rectangle) {
        sTCanvas.drawImage(image,rectangle);
    }

    private void onCanvasSizeChanged() {
        /*if(System.currentTimeMillis() > nextStateRedraw){
            processStateChangeCommand(actMementoID);
            nextStateRedraw = System.currentTimeMillis()+1000;
        }*/
    }

    public void onWidthChanged() {
        onCanvasSizeChanged();
    }

    public void onHeightChanged() {
        onCanvasSizeChanged();
    }

    public void setColor(Color color) {
        currentColor = color;
        sTCanvas.setColor(color);
    }

    public void setLineWidth(int lineWidth) {
        currentLineWidth = lineWidth;
        sTCanvas.setLineWidth(lineWidth);
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

    public void deleteAllMementoAfter(UUID mementoID) {
        stateCaretaker.cleanupAfterStateInsertion(mementoID);
    }

    public void insertRemoteMementoAfter(UUID id, ArrayList<Command> commands, boolean link, UUID creatorID, UUID after, UUID before) {
        if(stateCaretaker.hasMememnto(id)){
            logger.info("remote memento dropped: "+id.toString());
            return;
        }
        logger.info("inserting remote memento (ID:"+id.toString()+" from "+creatorID);
        try { mementoAddSemaphore.acquire(); } catch (InterruptedException e) { e.printStackTrace(); }
        StateMemento memento = new StateMemento(id,creatorID,link);
        memento.addCommands(commands);
        memento.setPreviousMementoID(after);
        memento.setNextMementoID(before);
        try{
            stateCaretaker.addMemento(memento, link);
        } catch (StateChainInconsistencyException|NotFoundException e) {
            e.printStackTrace();
            handleStateChainInconsistencyException();
        }

        actMementoID = memento.getId();
        processStateChangeCommand(actMementoID);
        mementoAddSemaphore.release();
    }
//////////////////////////////////////private section////////////////////////////////////////

    private StateMemento insertNewMementoAfterActual(boolean link) {//link: kell-e láncolni az új mementót a régivel (clear command után nem szabad)
        try { mementoAddSemaphore.acquire(); } catch (InterruptedException e) { e.printStackTrace(); }
        StateMemento memento = stateOriginator.createMemento();
        memento.setBackLinked(link);
        //amin éppen vagy az az utolsó-e
        try {
            if (actMementoID.equals(stateCaretaker.getLastMementoID())) {
                memento.setPreviousMementoID(actMementoID);
                memento.setNextMementoID(Constants.getEndChainUUID());
                stateCaretaker.addMemento(memento, link);
            } else {
                stateCaretaker.addMementoWCleanup(memento, actMementoID,
                        stateCaretaker.getMementoByID(actMementoID).getNextMementoID(), link);
                NetworkService.getInstance().sendDeleteAfterSignal(UserID.getInstance().getUserID(),canvasID,
                        memento.getPreviousMementoID());
            }
        } catch (StateChainInconsistencyException|NotFoundException e){
            e.printStackTrace();
            handleStateChainInconsistencyException();
        }

        actMementoID = memento.getId();
        mementoAddSemaphore.release();
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
        Command changeStateCommand = new ChangeStateCommand(this,UserID.getInstance().getUserID(),mementoID);
        NetworkService.getInstance().propagateCommandDownwards(changeStateCommand);
        NetworkService.getInstance().propagateCommandUpwards(changeStateCommand);
    }

    private void restoreMemento(StateMemento memento) {
        commandExecutorThread.addCommandToCommandQueue(new ClearCommand(this, UserID.getInstance().getUserID(),
                null,null,null));
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

}
