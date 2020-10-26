package com.sharedtable.controller;

import com.sharedtable.controller.commands.Command;

import java.util.ArrayList;
import java.util.UUID;

/*
 * A state originator a memento desgin pattern része. Ő felel a commandok valós időben történő összegyűjtséséért
 * majd mementóvá formálásáért, amint egy mementó lezárultnak minősül.
 * */

public class StateOriginator {

    private ArrayList<Command> currentCommandList = new ArrayList<>();
    private UUID nextMementoID;

    public StateOriginator() {
        nextMementoID = UUID.randomUUID();
    }

    public StateMemento createMemento() {
        StateMemento stateMemento = new StateMemento(nextMementoID,UserID.getInstance().getUserID(),true);
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


}
