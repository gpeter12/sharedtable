package controller;

import controller.commands.ChangeStateCommand;
import controller.commands.ClearCommand;
import controller.commands.CommandID;
import controller.commands.DrawLineCommand;
import controller.controllers.CanvasController;

public class CommandFactory {

    public static Command getCommand(String[] data, CanvasController canvasController) {
        String[] splittedData = data;
        int commandIDint = Integer.parseInt(splittedData[1]);
        CommandID commandID = CommandID.values()[commandIDint];
        switch (commandID) {
            case DrawLineCommand:
                return new DrawLineCommand(canvasController.getMainCanvas(), splittedData);
            case ClearCommand:
                ClearCommand c = new ClearCommand(canvasController,splittedData);
                c.setRemote(true);
                return c;
            case ChangeStateCommand:
                return new ChangeStateCommand(canvasController,splittedData);
        }
        throw new RuntimeException("Invalid Command recivied");
    }

}
