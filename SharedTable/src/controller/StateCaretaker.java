package controller;

import java.util.ArrayList;
import java.util.UUID;

public class StateCaretaker {

    public StateCaretaker() {

    }

    public void addMemento(StateMemento stateMemento) {
        mementos.get(mementos.size()-1).setNextMemento(stateMemento);
        mementos.add(stateMemento);
    }

    public void addMemento(StateMemento stateMemento,UUID after) {
        System.out.println("-------------------");
        for(int i = 0; i<mementos.size(); i++) {
            System.out.println("ind: " + i + " "+mementos.get(i));
        }
        System.out.println("-------------------");


        int ind = getMementoIndexByID(after);

        mementos.get(ind).setNextMemento(stateMemento);
        for(int i=ind+1; i<mementos.size()-1; i++) {
            mementos.get(i).setPreviousMemento(mementos.get(i-1));
            mementos.get(i).setNextMemento(mementos.get(i+1));
        }

        /*for(int i = ind+1; i<mementos.size(); i++) {
            mementos.remove(i);
        }*/
        mementos.add(ind,stateMemento);
    }

    private void resyncMementoChain(int ind) {
        for(int i=ind; i<mementos.size()-1; i++) {
            mementos.get(i).setPreviousMemento(mementos.get(i-1));
            mementos.get(i).setNextMemento(mementos.get(i+1));
        }
    }

    private void cleanupAfterStateInsertion(int ind) {

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

    private ArrayList<StateMemento> mementos = new ArrayList<>();
}
