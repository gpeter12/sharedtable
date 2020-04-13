package com.sharedtable.controller;

import com.sharedtable.view.MainView;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

public class StateCaretaker {

    public StateCaretaker() {
        logger = java.util.logging.Logger.getLogger(MainView.class.getName());
    }

    private void linkWithLastMemento(StateMemento stateMemento) {
        StateMemento actLastMemento = mementos.get(mementos.size() - 1);
        actLastMemento.setNextMemento(stateMemento);
        stateMemento.setPreviousMemento(actLastMemento);
    }

    public void addMemento(StateMemento stateMemento,boolean link) {
        logger.info("new memento added to the top: "+stateMemento.getId());
        if (mementos.size() > 0 && link)
            linkWithLastMemento(stateMemento);
        mementos.add(stateMemento);
    }

    public void addMemento(StateMemento stateMemento, UUID after, boolean link) {
        logger.info("new memento INSERTED: "+stateMemento.getId());
        cleanupAfterStateInsertion(after);
        addMemento(stateMemento,link);
    }

    public void printAllMementos() {
        System.out.println("-------------------");
        for (int i = 0; i < mementos.size(); i++) {
            System.out.println("ind: " + i + " " + mementos.get(i).getId());
        }
        System.out.println("-------------------");
    }

    public boolean hasMememnto(UUID id) {
        for(StateMemento act : mementos) {
            if(act.getId().equals(id))
                return true;
        }
        return false;
    }



    private void cleanupAfterStateInsertion(UUID wantedMementoID) {
        logger.info("cleaning up from top until reach "+wantedMementoID);
        while (!(getMementoByIndex(mementos.size() - 1).getId().equals(wantedMementoID))) {
            logger.info("removing #"+(mementos.size() - 1)+" memento due to insertion between 2 mementos.");
            mementos.remove(getMementoByIndex(mementos.size() - 1));
            if(mementos.size()==0) {
                throw new RuntimeException("target memento not found, all mementos were removed.");
            }
        }
    }

    public StateMemento getMementoByIndex(int index) {
        if (index < 0)
            return mementos.get(0);
        if (index > mementos.size() - 1)
            return mementos.get(mementos.size() - 1);
        return mementos.get(index);
    }


    public int getMementoIndexByID(UUID id) {
        for (int i = 0; i < mementos.size(); i++) {
            if (mementos.get(i).getId().equals(id))
                return i;
        }
        throw new RuntimeException("Memento index not found by ID");
    }

    public StateMemento getMementoByID(UUID id) {
        for (int i = 0; i < mementos.size(); i++) {
            if (mementos.get(i).getId().equals(id))
                return mementos.get(i);
        }
        throw new RuntimeException("Memento not found by ID");
    }

    public ArrayList<StateMemento> getMementos() {return mementos;}

    public UUID getLastMementoID() {
        return mementos.get(mementos.size() - 1).getId();
    }

    private ArrayList<StateMemento> mementos = new ArrayList<>();
    private Logger logger;
}
