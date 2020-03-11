package controller;

import controller.controllers.CanvasController;
import javafx.scene.input.KeyEvent;

public class KeyboardEventHandler {
    public KeyboardEventHandler(CanvasController canvasController) {
        this.canvasController = canvasController;
    }

    public void handleEvent(KeyEvent event) {

        //redo keycombo
        if (event.isControlDown() && isZDown(event) && !event.isShiftDown()) {

            System.out.println("UNDOOOOO");
            canvasController.undo();
        }

        //undo keycombo
        if (event.isControlDown() && event.isShiftDown() && isZDown(event)) {
            System.out.println("REDOOOOO");
            canvasController.redo();

        }
    }

    private boolean isZDown(KeyEvent event) {
        return event.getText().equals("Z") ||
                event.getCharacter().equals("z") ||
                event.getText().equals("\u001A");
    }

    CanvasController canvasController;
}
