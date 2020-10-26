package com.sharedtable.controller;

import com.sharedtable.Constants;
import com.sharedtable.controller.commands.Command;
import com.sharedtable.controller.commands.CommandFactory;
import com.sharedtable.controller.commands.DrawImageCommand;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

/*
 * A mementók egy láncot alkotnak azáltal, hogy tárolják az őket megelőzők ID-ját
 * és címét. így aztán mikor egy adott state-et kell kirajzolni a canvasra, képesek
 * a saját command-listjük mellett az összes őket megelőző state-ek commandjait is visszaadni.
 * De alapból mindegyik state csak az őt megelőzőhöz képest történt változásokat tartalmazza
 * */

public class StateMemento {

    private UUID creatorID;
    private StateMemento previousMemento;
    private StateMemento nextMemento;
    private UUID id;
    private UUID previousMementoID;
    private UUID nextMementoID;
    private ArrayList<Command> commands = new ArrayList<>();
    private boolean isBackLinked;

    public StateMemento(UUID id, UUID creatorID, boolean isBackLinked) {
        this.id = id;
        this.creatorID = creatorID;
        this.isBackLinked = isBackLinked;
    }

    public StateMemento(StateMemento inp, Logger logger) {
        this.id = inp.id;
        this.creatorID = inp.creatorID;
        this.isBackLinked = inp.isBackLinked;
        this.previousMementoID = inp.previousMementoID;
        this.nextMementoID = inp.nextMementoID;
        for(Command act : inp.commands) {
            Command actCopy = CommandFactory.getCommand(act.toString().split(";"),logger);
            if(actCopy instanceof DrawImageCommand) {
                ((DrawImageCommand) actCopy).setImage(((DrawImageCommand) act).getImageBytes());
            }
            this.commands.add(actCopy);
        }
    }

    public void setNewCanvasControllerForCommands(CanvasController canvasController) {
        for(Command act : commands) {
            act.setCanvasController(canvasController);
        }
    }

    public void addCommands(ArrayList<Command> commands) {
        for (Command act : commands) {
            if(commands.size()>0 && !act.getCreatorID().equals(commands.get(commands.size()-1).getCreatorID())){
                throw new IllegalStateException("memento must only have one author!");
            }
            this.commands.add(act);
        }
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }

    private UUID convertNullUUIDToNil(UUID inp) {
        if(inp == null)
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        return inp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateMemento that = (StateMemento) o;
        return id.equals(that.id) &&
                previousMementoID.equals(that.previousMementoID) &&
                nextMementoID.equals(that.nextMementoID) &&
                commands.equals(that.commands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, previousMementoID, nextMementoID, commands);
    }

    public ArrayList<Command> getAllCommandsWPrev() {
        ArrayList<Command> ret = new ArrayList<>();
        if (getPreviousMemento() != null)
            ret.addAll(getPreviousMemento().getAllCommandsWPrev());
        ret.addAll(getCommands());
        return ret;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public StateMemento getPreviousMemento() {
        return previousMemento;
    }

    public void setPreviousMemento(StateMemento previousMemento) {
        this.previousMemento = previousMemento;
        this.previousMementoID = previousMemento.getId();
    }

    public StateMemento getNextMemento() {
        return nextMemento;
    }

    public void setNextMemento(StateMemento nextMemento) {
        if(nextMemento == null){
            this.nextMemento = new StateMemento(Constants.getEndChainUUID(),null,true);
            this.nextMementoID = Constants.getEndChainUUID();
        } else {
            this.nextMementoID = nextMemento.getId();this.nextMemento = nextMemento;
        }

    }

    public UUID getPreviousMementoID() {
        return previousMementoID;
    }

    public UUID getNextMementoID() {
        return nextMementoID;
    }

    public UUID getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(UUID creatorID) {
        this.creatorID = creatorID;
    }

    public void setPreviousMementoID(UUID previousMementoID) {
        this.previousMementoID = previousMementoID;
    }

    public void setNextMementoID(UUID nextMementoID) {
        this.nextMementoID = nextMementoID;
    }

    public boolean isBackLinked() {
        return isBackLinked;
    }

    public void setBackLinked(boolean backLinked) {
        isBackLinked = backLinked;
    }

    @Override
    public String toString() {
        return "StateMemento{" +
                "creatorID=" + creatorID +
                //", previousMemento=" + previousMemento +
                //", nextMemento=" + nextMemento +
                ", id=" + id +
                ", previousMementoID=" + previousMementoID +
                ", nextMementoID=" + nextMementoID +
                ", commands=" + commands +
                ", isBackLinked=" + isBackLinked +
                '}';
    }
}
