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
    private ArrayList<Command> commands;
}
