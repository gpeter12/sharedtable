package controller.commands;

import controller.Command;
import view.MainCanvas;

import java.util.ArrayList;

public class ResyncCommand implements Command {

    public ResyncCommand(MainCanvas mainCanvas, ArrayList<Command> commands) {
        this.mainCanvas = mainCanvas;
        this.commands = deepCopyCommands(commands);
    }

    @Override
    public void execute() {
        mainCanvas.clear();
    }

    @Override
    public void deepCopy(Command command) {

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

    MainCanvas mainCanvas;
    ArrayList<Command> commands;
}