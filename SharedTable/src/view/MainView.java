package view;

import controller.ConnectWindowController;
import controller.KeyboardEventHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;


public class MainView extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("MainView.fxml"));
        primaryStage.setTitle("Shared Table");
        Scene scene = new Scene(root, 1366, 768);
        primaryStage.setScene(scene);
        primaryStage.show();

        MainCanvas mainCanvas = (MainCanvas)scene.lookup("#canvas");
        KeyboardEventHandler keyboardEventHandler = new KeyboardEventHandler(mainCanvas);
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED,
                event-> {
                    keyboardEventHandler.handleEvent(event);
                });
        initConnectWindow();
    }

    private void initConnectWindow() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ConnectWindow.fxml"));
        Parent parent = fxmlLoader.load();
        Scene scene = new Scene(parent, 600, 100);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }


    //TODO #1 Memento állapottárolásra (Caretaker, originator) DONE
        //Caretaker: Holds the array list of commands
        //Originator: Collects data and Creates mementoes from them
            // and unboxes mementoes
    //TODO #1.1 láncolt lista RAM effektivitásért DONE
    //TODO #1.2 viszavonási idővonal problémája DONE
    //TODO #2 blocking bufferlista a canvashoz
    //TODO  Threading
    //TODO #3 Basic connection élő rajzolással
    //TODO #4 login utáni szinkronbahozás
    //TODO #5 ha egy csomóbont kiszáll, megpróbál sorrendben csatlakozni bármely más klienshez
    //TODO #6 Bármilyen deszinkronizációs hiba esetén a legnegyobb IP vel rendelkező gép mester mementó listát küld szét a reszinkronizációhoz

//TODO #5 automatic lock conflict feloldás (akinek nagyobb az IP-je az kapja a lockot)
}

/*
------------CONSIDERATIONS------------
#1: lehetne effekívebb kapcsolati fa
#2: lehetne UPnP helyett valami megbízhatóbbat
 */



/*// Set line width
        gc.setLineWidth(2.0);
        // Set fill color
        gc.setFill(Color.RED);
        // Draw a rounded Rectangle
        gc.strokeRoundRect(10, 10, 50, 50, 10, 10);
        // Draw a filled rounded Rectangle
        gc.fillRoundRect(100, 10, 50, 50, 10, 10);
        // Change the fill color
        gc.setFill(Color.BLUE);
        // Draw an Oval
        gc.strokeOval(10, 70, 50, 30);
        // Draw a filled Oval
        gc.fillOval(100, 70, 50, 30);
        // Draw a Line
        gc.strokeLine(200, 50, 300, 50);
        // Draw an Arc
        gc.strokeArc(320, 10, 50, 50, 40, 80, ArcType.ROUND);
        // Draw a filled Arc
        gc.fillArc(320, 70, 50, 50, 00, 120, ArcType.OPEN);
        /*root.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;");*/

