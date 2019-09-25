package controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class StateMemento implements Serializable {

    public StateMemento() {
        id = UUID.randomUUID();
    }

    public void addCommands(ArrayList<Command> commands) {
        this.commands = commands;
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }

    public UUID getId() {
        return id;
    }

    private UUID id;

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
        this.nextMemento = nextMemento;
        this.nextMementoID = nextMemento.getId();
    }


    public UUID getPreviousMementoID() {
        return previousMementoID;
    }

    public UUID getNextMementoID() {
        return nextMementoID;
    }


    private StateMemento previousMemento;
    private StateMemento nextMemento;

    private UUID previousMementoID;
    private UUID nextMementoID;
    private ArrayList<Command> commands;
}
