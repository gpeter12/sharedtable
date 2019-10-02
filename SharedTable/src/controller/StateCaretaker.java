package controller;

import java.util.ArrayList;
import java.util.UUID;

public class StateCaretaker {

    public StateCaretaker() {

    }

    private void linkWithLastMemento(StateMemento stateMemento) {
        mementos.get(mementos.size()-1).setNextMemento(stateMemento);
    }

    public void addMemento(StateMemento stateMemento) {
        if(mementos.size()>0)
            linkWithLastMemento(stateMemento);
        mementos.add(stateMemento);
    }

    private void printAllMementos() {
        System.out.println("-------------------");
        for(int i = 0; i<mementos.size(); i++) {
            System.out.println("ind: " + i + " "+mementos.get(i).getId());
        }
        System.out.println("-------------------");
    }

    public void addMemento(StateMemento stateMemento,UUID after) {
        int ind = getMementoIndexByID(after);

        mementos.get(ind).setNextMemento(stateMemento);
        //relinkMementoChain(ind);
        //cleanupAfterStateInsertion(ind);

        mementos.add(ind,stateMemento);
        printAllMementos();
    }


    private void relinkMementoChain(int ind) {
        for(int i=ind; i<mementos.size()-1; i++) {
            mementos.get(i).setPreviousMemento(mementos.get(i-1));
            mementos.get(i).setNextMemento(mementos.get(i+1));
        }
    }

    private void cleanupAfterStateInsertion(int ind) {
        for(int i=ind; i<mementos.size()-1; i++) {
            mementos.remove(i);
        }
    }

    public StateMemento getMementoByIndex(int index) {
        if(index < 0)
            return mementos.get(0);
        if(index > mementos.size()-1)
            return mementos.get(mementos.size()-1);
        return mementos.get(index);
    }



    public int getMementoIndexByID(UUID id) {
        for(int i=0; i<mementos.size(); i++) {
            if( mementos.get(i).getId().equals(id))
                return i;
        }
        throw new RuntimeException("Memento index not found by ID");
    }

    public StateMemento getMementoByID(UUID id) {
        for(int i=0; i<mementos.size(); i++) {
            if( mementos.get(i).getId().equals(id))
                return mementos.get(i);
        }
        throw new RuntimeException("Memento not found by ID");
    }

    public UUID getLastMementoID() {
        return mementos.get(mementos.size()-1).getId();
    }

    private ArrayList<StateMemento> mementos = new ArrayList<>();
}
