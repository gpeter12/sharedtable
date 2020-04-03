package com.sharedtable.controller.commands;

import com.sharedtable.controller.TabController;

public class CommandFactory {

    public static Command getCommand(String[] data) {
        String[] splittedData = data;
        int commandIDint = Integer.parseInt(splittedData[1]);
        CommandTypeID commandTypeID = CommandTypeID.values()[commandIDint];
        switch (commandTypeID) {
            case DrawLineCommand:
                return setCanvasController(new DrawLineCommand(splittedData));
            case DrawRectangleCommand:
                return setCanvasController(new DrawRectangleCommand(splittedData));
            case ClearCommand:
                ClearCommand c = (ClearCommand)setCanvasController(new ClearCommand(splittedData));
                c.setRemote(true);
                return c;
            case ChangeStateCommand:
                return setCanvasController(new ChangeStateCommand(splittedData));
        }
        throw new RuntimeException("Invalid Command recivied");
    }

    public static Command setCanvasController(Command command) {
        command.setCanvasController(TabController.getCanvasController(command.getCanvasID()));
        return command;
    }

}
