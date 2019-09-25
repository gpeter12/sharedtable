package controller;

import javafx.scene.input.KeyEvent;
import view.MainCanvas;

public class KeyboardEventHandler {
    public KeyboardEventHandler(MainCanvas mainCanvas) {
        this.mainCanvas = mainCanvas;
    }

    public void handleEvent(KeyEvent event) {

        //redo keycombo
        if(event.isControlDown() && isZDown(event) && !event.isShiftDown()) {
            System.out.println("REDOOOOO");
            mainCanvas.redo();
        }

        //undo keycombo
        if(event.isControlDown() && event.isShiftDown() && isZDown(event)) {
            System.out.println("UNDOOOOO");
            mainCanvas.undo();
        }
    }

    private boolean isZDown(KeyEvent event) {
        return event.getText().equals("Z") ||
                event.getCharacter().equals("z") ||
                event.getText().equals("\u001A");
    }

    MainCanvas mainCanvas;
}
