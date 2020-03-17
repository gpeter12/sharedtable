package com.sharedtable.controller;

import com.sharedtable.controller.commands.ChangeStateCommand;
import com.sharedtable.controller.commands.ClearCommand;
import com.sharedtable.controller.commands.CommandID;
import com.sharedtable.controller.commands.DrawLineCommand;
import com.sharedtable.controller.controllers.CanvasController;

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
