package controller;

import java.util.ArrayList;

public class StateOriginator {

    public StateOriginator(){
    }

    public StateMemento createMemento(){
        StateMemento stateMemento = new StateMemento();
        stateMemento.addCommands(currentCommandList);
        //currentCommandList.clear();
        currentCommandList = deepCopyCommands(currentCommandList);
        return stateMemento;
    }

    private ArrayList<Command> deepCopyCommands(ArrayList<Command> cmds) {
        ArrayList<Command> ret = new ArrayList<>();
        for(Command act : cmds) {
            Command command = new DrawLineCommand();
            command.deepCopy(act);
            ret.add(command);
        }
        return ret;
    }

    public void addCommand(Command command) {

        currentCommandList.add(command);
        System.out.println("command added  " +command.toString());
    }

    private ArrayList<Command> currentCommandList = new ArrayList<>();
}
