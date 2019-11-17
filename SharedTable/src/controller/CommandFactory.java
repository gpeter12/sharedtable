package controller;

import controller.commands.DrawLineCommand;
import view.MainCanvas;

public class CommandFactory {

    public static Command getCommand(String data, MainCanvas mainCanvas) {
        String[] splittedData = data.split(";");
        int commandType = Integer.parseInt(splittedData[0]);
        DrawingMode drawingMode = DrawingMode.values()[commandType];
        switch(drawingMode) {
            case ContinousLine:
                return new DrawLineCommand(mainCanvas,splittedData);
            case Ellipse:
                throw new UnsupportedOperationException("Ellipse is not supported yet.");
            case Rectangle:
                throw new UnsupportedOperationException("Rectangle is not supported yet.");
        }
        throw new RuntimeException("Invalid Command recivied");
    }

}
