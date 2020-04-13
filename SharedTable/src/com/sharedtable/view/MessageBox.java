package com.sharedtable.view;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;

public class MessageBox {
    private MessageBox() {

    }

    public static void showInformation(String header, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Információ");
            alert.setHeaderText(header);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void showWarning(String header, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Figyelmeztetés");
            alert.setHeaderText(header);
            alert.setContentText(message);
            alert.showAndWait();
        });

    }


    public static void showError(String header, String message) {
        Platform.runLater(() -> {
           Alert alert = new Alert(Alert.AlertType.ERROR);
           alert.setTitle("HIBA!");
           alert.setHeaderText(header);
           alert.setContentText(message);
           alert.showAndWait();
        });
    }

    public static void showSyncWindow() {
        Platform.runLater(() -> {
            syncProcessView = new SyncProcessView();
        });
    }

    public static void closeSyncWindow() {
        if(syncProcessView != null) {
            Platform.runLater(() -> {
                syncProcessView.closeWindow();
            });
        } else {
            throw new RuntimeException("SyncProcessView was not shown!");
        }
    }

    private static SyncProcessView syncProcessView = null;

    /*public static boolean showConfirmation(String header, String message) {
        Optional<ButtonType> result = null;
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Megerősítés szükséges!");
            alert.setHeaderText(header);
            alert.setContentText(message);

            result = alert.showAndWait();

        });
        if (result.get() == ButtonType.OK){
            return true;
        } else {
            return false;
        }
    }*/

    public static void showPopup(String message, int time, Stage stage) {
        Popup popup = new Popup();
        Label label = new Label(message);
        label.setStyle(" -fx-background-color: white; -fx-background-opacity: 1.0");
        popup.centerOnScreen();
        popup.getContent().add(label);

        label.setMinWidth(80);
        label.setMinHeight(50);
        popup.setAutoHide(true);
        setTimerForPopup(time,popup);
        popup.show(stage);
    }

    private static void setTimerForPopup(int time, Popup popup) {
        TimerTask task = new TimerTask() {
            public void run() {
                Platform.runLater(() -> {
                    popup.hide();
                });
            }
        };
        Timer timer = new Timer("Timer");
        long delay = time;
        timer.schedule(task, delay);
    }
}
