package com.sharedtable.controller;

import com.sharedtable.Constants;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

public class StateCaretaker {

    private ArrayList<StateMemento> stateChain = new ArrayList<>();
    private Logger logger;


    public StateCaretaker() {
        logger = java.util.logging.Logger.getLogger(MainViewController.class.getName());
    }

    public void addFirstMemento(StateMemento fistMemento) {
        stateChain.add(fistMemento);
    }

    private StateMemento getColliderMemento(StateMemento memento) {
        if(stateChain.size()<2)
            return null;
        int i = stateChain.size() - 1;
        while (i>0 && (!(stateChain.get(i).getPreviousMementoID().equals(memento.getPreviousMementoID())))) {
            i--;
        }
        if( stateChain.get(i).getNextMementoID().equals(memento.getNextMementoID())){
            return stateChain.get(i);
        }
        return null;
    }

    private void rechain() {
        stateChain.get(0).setPreviousMementoID(Constants.getNilUUID());
        stateChain.get(0).setNextMemento(stateChain.get(1));
        for(int i = 1; i< stateChain.size()-1; i++) {
            if(stateChain.get(i).isBackLinked()) {
                stateChain.get(i).setPreviousMemento(stateChain.get(i - 1));
            }
            stateChain.get(i).setNextMemento(stateChain.get(i+1));
        }
        stateChain.get(stateChain.size()-1).setNextMementoID(Constants.getEndChainUUID());
    }

    private void resolveMementoCollision(StateMemento toInsert,StateMemento collides, boolean link) {
        int index = getMementoIndexByID(collides.getId());
        if(toInsert.getId().compareTo(collides.getId()) == 1) {
            stateChain.add(index+1,toInsert);
            if(link)
                rechain();
        } else {
            stateChain.add(index,toInsert);
            if(link)
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
                stateChain.add(stateMemento);
            } else {
                if(!areTheyNeighbours(stateMemento.getPreviousMementoID(),stateMemento.getNextMementoID())) {
                    throw new StateChainInconsistencyException();
                }
                stateChain.add(getMementoIndexByID(stateMemento.getPreviousMementoID())+1,stateMemento);
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
        for (int i = 0; i < stateChain.size(); i++) {
            System.out.println("------>>ind: " + i + ". " + stateChain.get(i).getId());
            System.out.println(stateChain.get(i).getPreviousMementoID().toString());
            System.out.println(stateChain.get(i).getNextMementoID().toString());
            if(stateChain.get(i).getPreviousMemento() != null){
                System.out.println(stateChain.get(i).getPreviousMemento().getId().toString());
                if(!stateChain.get(i).getPreviousMemento().getId().equals(stateChain.get(i).getPreviousMementoID())){
                    System.out.println("!!NOT MATCHING!!");
                }
            }
            if(stateChain.get(i).getNextMemento() != null){
                System.out.println(stateChain.get(i).getNextMemento().getId().toString());
                if(!stateChain.get(i).getNextMemento().getId().equals(stateChain.get(i).getNextMementoID())){
                    System.out.println("!!NOT MATCHING!!");
                }
            }
            System.out.println("isBackLinked: "+ stateChain.get(i).isBackLinked());
        }
        System.out.println("-------------------");
    }

    public boolean hasMememnto(UUID id) {
        for(StateMemento act : stateChain) {
            if(act.getId().equals(id))
                return true;
        }
        return false;
    }



    public void cleanupAfterStateInsertion(UUID wantedMementoID) {
        logger.info("cleaning up from top until reach "+wantedMementoID);
        while (!(getMementoByIndex(stateChain.size() - 1).getId().equals(wantedMementoID))) {//azt már nem törli ki
            logger.info("wanted: "+wantedMementoID+" removing #"+(stateChain.size() - 1)+" with ID: "+(getMementoByIndex(stateChain.size() - 1)).getId()+" memento due to insertion between 2 mementos.");
            stateChain.remove(getMementoByIndex(stateChain.size() - 1));
            if(stateChain.size()==0) {
                throw new RuntimeException("target memento not found, all mementos were removed.");
            }
        }
        getLastMemento().setNextMementoID(Constants.getEndChainUUID());
        getLastMemento().setNextMemento(null);
    }

    public StateMemento getMementoByIndex(int index) {
        if (index < 0)
            return stateChain.get(0);
        if (index > stateChain.size() - 1)
            return stateChain.get(stateChain.size() - 1);
        return stateChain.get(index);
    }


    public int getMementoIndexByID(UUID id) throws NotFoundException {
        for (int i = 0; i < stateChain.size(); i++) {
            if (stateChain.get(i).getId().equals(id))
                return i;
        }
        printAllMementos();
        throw new NotFoundException("Memento index not found by ID: "+id);
    }

    public StateMemento getMementoByID(UUID id) throws NotFoundException {
        for (int i = 0; i < stateChain.size(); i++) {
            if (stateChain.get(i).getId().equals(id))
                return stateChain.get(i);
        }
        printAllMementos();
        throw new NotFoundException("Memento not found by ID: "+id);
    }

    public ArrayList<StateMemento> getMementos() {return stateChain;}

    public UUID getLastMementoID() {
        return stateChain.get(stateChain.size() - 1).getId();
    }

    public StateMemento getLastMemento() {
        return stateChain.get(stateChain.size() - 1);
    }

    public void clearStateChain() {
        stateChain.clear();
    }

}
