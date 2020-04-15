package com.sharedtable.controller;

import javafx.scene.input.KeyEvent;

public class KeyboardEventHandler {
    public KeyboardEventHandler(CanvasController canvasController) {
        this.canvasController = canvasController;
    }

    public void handleEvent(KeyEvent event) {

        //redo keycomboSys
        if(System.getProperty("os.name").contains("Windows")){
            if (event.isControlDown() && event.getText().equals("z")) {
                canvasController.undo();

            }
            //undo keycombo
            if (event.isControlDown() && event.getText().equals("y")) {
                canvasController.redo();

            }
        } else if(System.getProperty("os.name").contains("Linux")){
            if (event.isControlDown() && isZDown(event) && !event.isShiftDown()) {

                canvasController.undo();
            }

            //undo keycombo
            if (event.isControlDown() && event.isShiftDown() && isZDown(event)) {
                canvasController.redo();

            }
        }

    }

    private boolean isZDown(KeyEvent event) {
        return event.getText().equals("Z") ||
                event.getCharacter().equals("z") ||
                event.getText().equals("\u001A");
    }

    CanvasController canvasController;
}
