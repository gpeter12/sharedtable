package controller;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

/*
 * A mementók egy láncot alkotnak azáltal, hogy tárolják az őket megelőzők ID-ját
 * és címét. így aztán mikor egy adott state-et kell kirajzolni a canvasra, képesek
 * a saját command-listjük mellett az összes őket megelőző state-ek commandjait is visszaadni.
 * De alapból mindegyik state csak az őt megelőzőhöz képest történt változásokat tartalmazza
 * */

public class StateMemento {

    public StateMemento(UUID id) {
        this.id = id;
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

    public void addCommands(ArrayList<Command> commands) {
        for (Command act : commands) {
            this.commands.add(act);
        }
    }

    public ArrayList<Command> getCommands() {
        return commands;
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

    public ArrayList<Command> getAllCommands() {
        ArrayList<Command> ret = new ArrayList<>();
        if (getPreviousMemento() != null)
            ret.addAll(getPreviousMemento().getAllCommands());
        ret.addAll(getCommands());
        return ret;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public int getCommandNumber() {
        return commands.size();
    }


    private StateMemento previousMemento;
    private StateMemento nextMemento;

    private UUID previousMementoID;
    private UUID nextMementoID;
    private ArrayList<Command> commands = new ArrayList<>();
}
