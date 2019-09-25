package view;

import controller.KeyboardEventHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;


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

    }



    public static void main(String[] args) {


        launch(args);
    }


    //TODO #1 Memento állapottárolásra (Caretaker, originator)
        //Caretaker: Holds the array list of commands
        //Originator: Collects data and Creates mementoes from them
            // and unboxes mementoes
    //TODO #1.1 láncolt lista RAM effektivitásért
    //TODO #1.2 viszavonási idővonal problémája
    //TODO #2 Basic connection élő rajzolással
    //TODO #3 login utáni szinkronbahozás
    //TODO #4 automatic lock conflict feloldás (akinek nagyobb az IP-je az kapja a lockot)
    //TODO #4.1 Bármilyen deszinkronizációs hiba esetén a legnegyobb IP vel rendelkező gép mester mementó listát küld szét a reszinkronizációhoz
    //TODO #5 ha egy csomóbont kiszáll, megpróbál sorrendben csatlakozni bármely más klienshez

}


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

