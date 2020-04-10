package com.sharedtable.controller.commands;

import com.sharedtable.controller.TabController;
import com.sharedtable.model.ArrayPrinter;

import java.util.UUID;
import java.util.logging.Logger;

public class CommandFactory {

    public static Command getCommand(String[] data, Logger logger) {
        String[] splittedData = data;
        try{
            UUID.fromString(data[0]);
        } catch (Exception e){
            logger.severe("getCommand() garbage dropped! "+ArrayPrinter.printStringArray(data));
            return null;
        }
        int commandIDint = Integer.parseInt(splittedData[1]);
        CommandTypeID commandTypeID = CommandTypeID.values()[commandIDint];
        switch (commandTypeID) {
            case DrawLineCommand:
                return setCanvasController(new DrawLineCommand(splittedData));
            case DrawRectangleCommand:
                return setCanvasController(new DrawRectangleCommand(splittedData));
            case DrawTriangleCommand:
                return setCanvasController(new DrawTriangleCommand(splittedData));
            case DrawEllipseCommand:
                return setCanvasController(new DrawEllipseCommand(splittedData));
            case DrawImageCommand:
                return setCanvasController(new DrawImageCommand(splittedData));
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

    public static boolean isDrawImageCommand(String[] splittedData) {
        int commandIDint = Integer.parseInt(splittedData[1]);
        if(CommandTypeID.DrawImageCommand == CommandTypeID.values()[commandIDint])
            return true;
        return false;
    }

}
