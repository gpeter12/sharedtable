package controller;

import java.util.ArrayList;
import java.util.UUID;

public class StateCaretaker {

    public StateCaretaker() {

    }

    public void addMemento(StateMemento stateMemento) {
        mementos.add(stateMemento);
    }

    public void addMemento(StateMemento stateMemento,UUID after) {
        System.out.println("-------------------");
        for(int i = 0; i<mementos.size(); i++) {
            System.out.println("ind: " + i + " "+mementos.get(i));
        }
        System.out.println("-------------------");
        int ind = getMementoIndexByID(after);
        /*for(int i = ind+1; i<mementos.size(); i++) {
            mementos.remove(i);
        }*/
        mementos.add(ind,stateMemento);
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
        return -1;
    }

    private ArrayList<StateMemento> mementos = new ArrayList<>();
}
