package com.sharedtable.controller;

import com.sharedtable.Constants;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

public class StateCaretaker {

    private ArrayList<StateMemento> mementos = new ArrayList<>();
    private Logger logger;


    public StateCaretaker() {
        logger = java.util.logging.Logger.getLogger(MainViewController.class.getName());
    }

    public void addFirstMemento(StateMemento fistMemento) {
        mementos.add(fistMemento);
    }

    private StateMemento getColliderMemento(StateMemento memento) {
        if(mementos.size()<2)
            return null;
        int i = mementos.size() - 1;
        while (i>0 && (!(mementos.get(i).getPreviousMementoID().equals(memento.getPreviousMementoID())))) {
            i--;
        }
        if( mementos.get(i).getNextMementoID().equals(memento.getNextMementoID())){
            return mementos.get(i);
        }
        return null;
    }

    private void rechain() {
        mementos.get(0).setPreviousMementoID(Constants.getNilUUID());
        mementos.get(0).setNextMemento(mementos.get(1));
        for(int i=1; i<mementos.size()-1; i++) {
            if(mementos.get(i).isBackLinked()) {
                mementos.get(i).setPreviousMemento(mementos.get(i - 1));
            }
            mementos.get(i).setNextMemento(mementos.get(i+1));
        }
        mementos.get(mementos.size()-1).setNextMementoID(Constants.getEndChainUUID());
    }

    private void resolveMementoCollision(StateMemento toInsert,StateMemento collides, boolean link) {
        int index = getMementoIndexByID(collides.getId());
        if(toInsert.getId().compareTo(collides.getId()) == 1) {
            mementos.add(index+1,toInsert);
            if(link)
                //linkMementoWithMemento(mementos.get(index),toInsert);
                rechain();
        } else {
            mementos.add(index,toInsert);
            if(link)
                //linkMementoWithMemento(toInsert,mementos.get(index));
                rechain();
        }
    }

    private boolean areTheyNeighbours(UUID a, UUID b) {
        return getLastMementoID().equals(a) &&
                b.equals(Constants.getEndChainUUID()) ||
                getMementoIndexByID(a)+1 == getMementoIndexByID(b) ||
                getMementoIndexByID(b)+1 == getMementoIndexByID(a);
    }
    private void linkMementoWithMemento(StateMemento a, StateMemento b) {
        a.setNextMemento(b);
        b.setPreviousMemento(a);
    }

    private boolean validateMementoIDs(StateMemento inp) {
        return !(inp.getPreviousMementoID() == null && !inp.isBackLinked() ||
        inp.getNextMementoID().equals(Constants.getNilUUID()) ||
        inp.getPreviousMementoID().equals(Constants.getEndChainUUID()));
    }

    public void addMemento(StateMemento stateMemento, boolean link) throws StateChainInconsistencyException {
        if(stateMemento.getPreviousMementoID().equals(getLastMementoID())){
            stateMemento.setNextMementoID(Constants.getEndChainUUID());
        }

        if(!validateMementoIDs(stateMemento)) {
            System.out.println(stateMemento.toString());
            printAllMementos();
            throw new IllegalArgumentException("invalid next or prev mementoID");
        }

        StateMemento colliderMemento = getColliderMemento(stateMemento);
        if(colliderMemento != null) {
            resolveMementoCollision(stateMemento,colliderMemento,link);
        }
        else {
            if(stateMemento.getNextMementoID().equals(Constants.getEndChainUUID())) {
                mementos.add(stateMemento);
            } else {
                if(!areTheyNeighbours(stateMemento.getPreviousMementoID(),stateMemento.getNextMementoID())) {
                    throw new StateChainInconsistencyException();
                }
                mementos.add(getMementoIndexByID(stateMemento.getPreviousMementoID())+1,stateMemento);
            }
            if (link) {
                linkMementoWithMemento(getMementoByID(stateMemento.getPreviousMementoID()), stateMemento);
            }
            if(stateMemento.getId().equals(getLastMementoID())) {
                stateMemento.setNextMementoID(Constants.getEndChainUUID());
            } else {
                linkMementoWithMemento(stateMemento,getMementoByID(stateMemento.getNextMementoID()));
            }

        }


        logger.info("new memento added to the top: "+stateMemento.getId());
    }

    public void addMementoWCleanup(StateMemento stateMemento, UUID after, UUID before, boolean link) throws StateChainInconsistencyException {
        stateMemento.setPreviousMementoID(after);
        stateMemento.setNextMementoID(before);
        addMemento(stateMemento,link);
        cleanupAfterStateInsertion(stateMemento.getId());
        logger.info("new memento INSERTED: "+stateMemento.getId());
    }

    public void printAllMementos() {
        System.out.println("-------------------");
        for (int i = 0; i < mementos.size(); i++) {
            System.out.println("------>>ind: " + i + ". " + mementos.get(i).getId());
            System.out.println(mementos.get(i).getPreviousMementoID().toString());
            System.out.println(mementos.get(i).getNextMementoID().toString());
            if(mementos.get(i).getPreviousMemento() != null){
                System.out.println(mementos.get(i).getPreviousMemento().getId().toString());
                if(!mementos.get(i).getPreviousMemento().getId().equals(mementos.get(i).getPreviousMementoID())){
                    System.out.println("!!NOT MATCHING!!");
                }
            }
            if(mementos.get(i).getNextMemento() != null){
                System.out.println(mementos.get(i).getNextMemento().getId().toString());
                if(!mementos.get(i).getNextMemento().getId().equals(mementos.get(i).getNextMementoID())){
                    System.out.println("!!NOT MATCHING!!");
                }
            }
            System.out.println("isBackLinked: "+mementos.get(i).isBackLinked());
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



    public void cleanupAfterStateInsertion(UUID wantedMementoID) {
        logger.info("cleaning up from top until reach "+wantedMementoID);
        while (!(getMementoByIndex(mementos.size() - 1).getId().equals(wantedMementoID))) {//azt már nem törli ki
            logger.info("wanted: "+wantedMementoID+" removing #"+(mementos.size() - 1)+" with ID: "+(getMementoByIndex(mementos.size() - 1)).getId()+" memento due to insertion between 2 mementos.");
            mementos.remove(getMementoByIndex(mementos.size() - 1));
            if(mementos.size()==0) {
                throw new RuntimeException("target memento not found, all mementos were removed.");
            }
        }
        getLastMemento().setNextMementoID(Constants.getEndChainUUID());
        getLastMemento().setNextMemento(null);
    }

    public StateMemento getMementoByIndex(int index) {
        if (index < 0)
            return mementos.get(0);
        if (index > mementos.size() - 1)
            return mementos.get(mementos.size() - 1);
        return mementos.get(index);
    }


    public int getMementoIndexByID(UUID id) throws NotFoundException {
        for (int i = 0; i < mementos.size(); i++) {
            if (mementos.get(i).getId().equals(id))
                return i;
        }
        printAllMementos();
        throw new NotFoundException("Memento index not found by ID: "+id);
    }

    public StateMemento getMementoByID(UUID id) throws NotFoundException {
        for (int i = 0; i < mementos.size(); i++) {
            if (mementos.get(i).getId().equals(id))
                return mementos.get(i);
        }
        printAllMementos();
        throw new NotFoundException("Memento not found by ID: "+id);
    }

    public ArrayList<StateMemento> getMementos() {return mementos;}

    public UUID getLastMementoID() {
        return mementos.get(mementos.size() - 1).getId();
    }

    public StateMemento getLastMemento() {
        return mementos.get(mementos.size() - 1);
    }

    public void clearStateChain() {
        mementos.clear();
    }

}
