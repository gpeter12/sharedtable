package controller;

import java.util.ArrayList;
import java.util.UUID;

/*
 * A state originator a memento desgin pattern része. Ő felel a commandok valós időben történő összegyűjtséséért
 * majd mementóvá formálásáért, amint egy mementó lezárultnak minősül.
 * */

public class StateOriginator {

    public StateOriginator() {
        nextMementoID = UUID.randomUUID();
    }

    public StateMemento createMemento() {
        StateMemento stateMemento = new StateMemento(nextMementoID);
        stateMemento.addCommands(currentCommandList);
        currentCommandList.clear();
        nextMementoID = UUID.randomUUID();
        return stateMemento;
    }

    public void addCommand(Command command) {
        currentCommandList.add(command);
    }

    public UUID getNextMementoID() {return nextMementoID;}

    private ArrayList<Command> currentCommandList = new ArrayList<>();
    private UUID nextMementoID;
}
