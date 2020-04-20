package com.sharedtable.controller;

import com.sharedtable.controller.commands.Command;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * A state originator a memento desgin pattern része. Ő felel a commandok valós időben történő összegyűjtséséért
 * majd mementóvá formálásáért, amint egy mementó lezárultnak minősül.
 * */

public class StateOriginator {

    public StateOriginator() {
        nextMementoID = UUID.randomUUID();
    }

    public StateMemento createMemento() {
        StateMemento stateMemento = new StateMemento(nextMementoID,UserID.getUserID());
        stateMemento.addCommands(currentCommandList);
        currentCommandList.clear();
        nextMementoID = UUID.randomUUID();
        return stateMemento;
    }

    public boolean isCommandBufferEmpty() {return currentCommandList.isEmpty();}

    public void addCommand(Command command) {
        if(currentCommandList.size()>0 && !command.getCreatorID().equals(currentCommandList.get(currentCommandList.size()-1).getCreatorID())){
            throw new IllegalStateException("memento must only have one author!");
        }
        currentCommandList.add(command);
    }

    public UUID getNextMementoID() {return nextMementoID;}

    private CopyOnWriteArrayList<Command> currentCommandList = new CopyOnWriteArrayList<>();
    private UUID nextMementoID;
}
