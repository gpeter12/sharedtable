package controller.commands;

import controller.Command;
import view.MainCanvas;

public class ClearCommand implements Command {

    public ClearCommand(MainCanvas mainCanvas) {
        this.mainCanvas = mainCanvas;
    }

    MainCanvas mainCanvas;

    @Override
    public void execute() {
        mainCanvas.clear();
    }

    @Override
    public void deepCopy(Command command) {

    }
}
