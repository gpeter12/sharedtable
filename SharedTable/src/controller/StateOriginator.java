package controller;

import controller.commands.DrawLineCommand;

import java.util.ArrayList;

public class StateOriginator {

    public StateOriginator(){

    }

    public StateMemento createMemento(){
        StateMemento stateMemento = new StateMemento();
        stateMemento.addCommands(deepCopyCommands(currentCommandList));
        currentCommandList.clear();
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
    }

    private ArrayList<Command> currentCommandList = new ArrayList<>();
}
