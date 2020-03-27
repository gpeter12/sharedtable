package com.sharedtable.view;

import com.sharedtable.controller.KeyboardEventHandler;
import com.sharedtable.controller.RemoteDrawLineCommandBufferHandler;
import com.sharedtable.controller.controllers.CanvasController;
import com.sharedtable.controller.controllers.ConnectWindowController;
import com.sharedtable.model.NetworkService;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;


public class MainView extends Application {

    @Override
    public void start(Stage primaryStage) {
        Parent root;
        try {
             root = FXMLLoader.load(getClass().getResource("MainView.fxml"));
        } catch (IOException e) {
            System.out.println("failed to get resource MainView.fxml");
            return;
        }
        primaryStage.setTitle("Shared Table (mode: "+startMode+")");
        Scene scene = new Scene(root, 640, 480);
        primaryStage.setScene(scene);
        primaryStage.show();

        MainCanvas mainCanvas = (MainCanvas) scene.lookup("#canvas");
        CanvasController canvasController = new CanvasController(mainCanvas);
        this.canvasController = canvasController;
        mainCanvas.initEventHandlers(canvasController);

        RemoteDrawLineCommandBufferHandler.setCanvasController(canvasController);

        //FOR DEBUG PURPUSES
        if(IP == null)
            IP = "127.0.0.1";
        if(port == -1) {
            if(startMode == 2)
            port = 2222;
            if(startMode == 0)
                port = 2223;
            if(startMode == 1)
                port = 2222;
        }

        if (startMode == 2) {
            new NetworkService(true, canvasController, 2222);
        } else if (startMode == 1) {
            new NetworkService(true, canvasController, 2223);
            //NetworkService.connect("127.0.0.1", 2223);f
            try {NetworkService.connect(IP, 2222);}
            catch (IOException e) {
                System.out.println("failed to connect in startMode 1");
            }
        } else if (startMode == 0) {
            new NetworkService(false, canvasController, 2224);
            //NetworkService.connect(IP, port);
            try {NetworkService.connect(IP, 2224);}
            catch (IOException e) {
                System.out.println("failed to connect in startMode 0");
            }
        }

        KeyboardEventHandler keyboardEventHandler = new KeyboardEventHandler(canvasController);
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED,
                event -> {
                    keyboardEventHandler.handleEvent(event);
                });
        //initConnectWindow();
    }

    @Override
    public void stop() {
        canvasController.stop();
        NetworkService.timeToStop();
    }

    @FXML
    public void onClearPressed(javafx.event.ActionEvent actionEvent) {
        canvasController.clearCanvas();
    }

    @FXML
    public void onUndoPressed(ActionEvent actionEvent) {
        canvasController.undo();
    }

    @FXML
    public void onRedoPressed(ActionEvent actionEvent) {
        canvasController.redo();
    }

    @FXML
    public void onListMementosPressed(ActionEvent actionEvent) {
        canvasController.printAllMementos();
    }

    @FXML
    public void onMakeCirclePressed(ActionEvent actionEvent) {
        try {NetworkService.connect("127.0.0.1", 2223);}
        catch (IOException e) {
            System.out.println("failed to conect in MakeCircle");
        }
    }

    @FXML
    public void onWriteAllKnownClientsPressed(ActionEvent actionEvent) {
        NetworkService.printClientList();
    }

    @FXML
    public void onPingClientPressed(ActionEvent actionEvent) {
        Scanner keyboard = new Scanner(System.in);
        UUID id = UUID.fromString(keyboard.nextLine());
        NetworkService.pingClient(id);
        NetworkService.pingClient(id);
        NetworkService.pingClient(id);
        NetworkService.pingClient(id);
        NetworkService.pingClient(id);
        NetworkService.pingClient(id);
        NetworkService.pingClient(id);
    }

    @FXML
    public void onViewClientListPressed(ActionEvent actionEvent) {
        new ClientsWindowView();
    }

    @FXML
    public void onConnectPressed(ActionEvent actionEvent) {
        new ConnectWindowView();
    }

    public static void main(String[] args) {
        startMode = Integer.parseInt(args[0]);
        if(args.length > 1){
            IP = args[1];
            port = Integer.parseInt(args[2]);
        }
        launch(args);
    }

    private static int startMode;
    private static String IP = null;
    private static int port =-1;
    private static CanvasController canvasController;


}


    //TODO #1 Memento állapottárolásra (Caretaker, originator) DONE
    //Caretaker: Holds the array list of commands
    //Originator: Collects data and Creates mementos from them
    // and unboxes mementos
    //TODO #1.1 "láncolt lista" RAM effektivitásért DONE
    //TODO #1.2 viszavonási idővonal problémája DONE
    //TODO #2 blocking bufferlista a canvashoz DONE
    //TODO #X Threading (with blocking queues) DONE
    //TODO #3 Basic connection élő rajzolással DONE
    //TODO #4 a clear command által létrehozott BlankMemento UUID-je aszinkronba kerül a többiekével. DONE
    //TODO #X megkeresni és be semaphorozni azokat a pontokat a command és memento handlingban amiket kell... DONE
    //TODO #X fában kör kialakulásának megakadályozása DONE
    //TODO #4 login utáni szinkronbahozás DONE
    //TODO ## Exception happened during sending plain textjava.net.SocketException: Socket closed DONE
    //TODO #5 befejezni a HandshakingInfo és NetworkClientEntity új fieldjeit (IP,nickname) DONE
    //TODO ## minden hálózatban levő kliens folyamatos nyilvántartása NetworkClientEntityTree-ban (kell az átcsatlakozáshoz)
    //TODO #6 ha egy csomópont kiszáll, megpróbál sorrendben csatlakozni bármely más klienshez
    //TODO #7 multi canvas
    //TODO #8 chat
    //TODO #9 PINGING

    //TODO #7 ellipszis
    //TODO #8 téglalap
    //TODO #9 háromszög
    //---------LOW PRIORITY-------------------------
    //TODO #X a ConnectWindow-ra kiírni a stconnect linket, és a link mezőt. súgógombok a linkek mellé.
    //TODO #X kiírni rögtön init után, ha nem lehet UPNP-n portot nyitni, és tájékoztatni a tűzfalról is
    //TODO #6 Bármilyen deszinkronizációs hiba esetén a legnegyobb IP vel rendelkező gép mester mementó listát küld szét a reszinkronizációhoz


//TODO #5 automatic lock conflict feloldás (akinek nagyobb az IP-je az kapja a lockot)

    /*
    ------------------------------SCENARIOS----------------------------------------------------
    SCEN #1: mi történik helyileg, ha valaki távol befejez egy rajzolást a rajzolásom alatt
          Válasz: az eddig bufferelt commandokból azonnal létrejön helyileg a saját mementóm és lezárul,
          az ő helyileg külön bufferelt commandjai ezután kerülnek egy újabb mementóba, majd azt is lezárjuk.
          (ezzel megőrizzük az időbeliséget, de nem mossuk össze a felhasználók commandjait egy mementóba)
    stateOriginator.getNextMementoID() időbelisége problémákat okozhat!!!
    -------------------------
    SCEN #2: mivan, ha akkor vonok vissza mikor valaki más rajzol?
          Válasz: a mementó váltás megtörténik, de a rajzoló mementó lezárásakor minden későbbi mementó törlődik, így
              Ő lesz a legújabb mementó az egér elengedésével.
    -------------------------
    SCEN #3: mivan ha valaki akkor clearel amikor valaki más rajzol, és a rajzolás tovább folyik?
          Válasz: a rajzoló mementó tényleges rögzítése a clear parancs utáni menetó rögzítése után történik meg,
                  így visszaállítható a törölt vonalrész, de elsőre eltűnik... a lejújabb mementó utén lesz rögzítve
    -------------------------
    SCEN #4: hogyan választom szét az egyszerre keletkező helyi és távoli drawLine commandokat -->
              RemoteDrawLineCommandBufferHandler DONE
    ------------------------
    SCEN#5
    két top levele cliens egy-egy fa tetején csatlakozik, mindekttőnek van egy rakás mementója,
        hogyan lesznek szinkronba hozva?


     */



/*
Párhuzamos rajzolásnál felmerül a hiba lehetőség, hogy lokális gépen egyel több command kerül
be a state be, amit egy másik kliens indított. Ez azért lehetséges, mert a CloseMementoCommandnak
a másik klienstől propagációs ideje van. Ebben az esetben azt a memento-t kell mindenkinek megtartania
amelyik több commandot tartalmaz. Így minden záráskor össze kell hasonlítani a lokálisan keletkezett
hashCode-ot a távolról érkező (close command által tartalmazottal) hashCode-al, és állítólagos
command számmal.
 */

/*
 *
 */

/*
------------CONSIDERATIONS------------
#1: lehetne effekívebb kapcsolati fa
#2: lehetne com.sharedtable.UPnP helyett valami megbízhatóbbat
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

