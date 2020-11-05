package com.sharedtable.view;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

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
            Logger.getAnonymousLogger().warning("sync window is null");
        }
    }

    private static SyncProcessView syncProcessView = null;

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
