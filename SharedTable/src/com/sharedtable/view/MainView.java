package com.sharedtable.view;


import com.sharedtable.controller.CanvasController;
import com.sharedtable.controller.UserID;
import com.sharedtable.controller.TabController;
import com.sharedtable.model.NetworkService;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.UUID;


public class MainView extends Application  {

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


        this.tabPane = (STTabPane)scene.lookup("#tabPane");
        new TabController(tabPane, primaryStage);


        this.colorPicker = (ColorPicker)scene.lookup("#colorPicker");
        colorPicker.setValue(Color.BLACK);
        setColorOnAllCanvases(Color.BLACK);


        this.lineWidthPicker = (ComboBox) scene.lookup("#lineWidthPicker");
        String availableWidths[] =
                { "6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","22","24","26","28","32","36","40" };
        lineWidthPicker.getItems().addAll(availableWidths);


        //FOR DEBUG PURPUSES
        if (IP == null)
            IP = "127.0.0.1";
        if (port == -1) {
            if (startMode == 2)
                port = 2222;
            if (startMode == 0)
                port = 2223;
            if (startMode == 1)
                port = 2222;
        }

        if (startMode == 2) {
            new NetworkService(true, 2222);
        } else if (startMode == 1) {
            new NetworkService(true, 2223);
            //NetworkService.connect("127.0.0.1", 2223);f
            try {
                NetworkService.connect(IP, 2222);
            } catch (IOException e) {
                System.out.println("failed to connect in startMode 1");
            }
        } else if (startMode == 0) {
            new NetworkService(true, 2224);
            try {
                NetworkService.connect(IP, 2223);
            } catch (IOException e) {
                System.out.println("failed to connect in startMode 0");
            }
        } else if (startMode == -1) {
            new NetworkService(true, 2225);
            try {
                NetworkService.connect(IP, 2223);
            } catch (IOException e) {
                System.out.println("failed to connect in startMode 4");
            }
        }

        //initConnectWindow();
    }

    @Override
    public void stop() {
        TabController.stop();
        NetworkService.timeToStop();
    }

    @FXML
    public void onClearPressed(javafx.event.ActionEvent actionEvent) {
        TabController.getActualCanvasControler().clearCanvas();
    }

    @FXML
    public void onUndoPressed(ActionEvent actionEvent) {
        TabController.getActualCanvasControler().undo();
    }

    @FXML
    public void onRedoPressed(ActionEvent actionEvent) {
        TabController.getActualCanvasControler().redo();
    }

    @FXML
    public void onViewClientListPressed(ActionEvent actionEvent) {
        new ClientsWindowView();
    }

    @FXML
    public void onConnectPressed(ActionEvent actionEvent) {
        new ConnectWindowView();
    }

    @FXML
    public void onCreateNewTabPressed(ActionEvent actionEvent) {
        UUID tabID = UUID.randomUUID();
        CreateTabView createTabView = new CreateTabView();
        TabController.createNewTab(tabID, createTabView.getController().getTabName());
        NetworkService.sendNewTabSignal(UserID.getUserID(),tabID,createTabView.getController().getTabName());
    }

    @FXML
    public void onTestConnectPressed(ActionEvent actionEvent) {
        try {
            new NetworkService(false, -1);
            NetworkService.connect(IP, 2223);
        } catch (IOException e) {
            System.out.println("failed to connect in startMode -2");
        }
    }

    @FXML
    public void onColorSelected(ActionEvent actionEvent) {
        setColorOnAllCanvases(colorPicker.getValue());
    }

    @FXML
    public void onLineWidthSelected(ActionEvent actionEvent) {
        setLineWidthOnAllCanvases(Integer.parseInt((String)lineWidthPicker.getValue()));
    }

    public static void main(String[] args) {
        startMode = Integer.parseInt(args[0]);
        if (args.length > 1) {
            IP = args[1];
            port = Integer.parseInt(args[2]);
        }
        launch(args);
    }

    private static void setColorOnAllCanvases(Color color) {
        for(CanvasController act : TabController.getAllCanvasControllers()) {
            act.setColor(color);
        }
    }

    private static void setLineWidthOnAllCanvases(int lineWidth) {
        for(CanvasController act : TabController.getAllCanvasControllers()) {
            act.setLineWidth(lineWidth);
        }
    }

    @FXML
    private static STTabPane tabPane;
    @FXML
    private static ColorPicker colorPicker;
    @FXML
    private static ComboBox lineWidthPicker;
    private static int startMode;
    private static String IP = null;
    private static int port = -1;



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
    //TODO ## ha nem sikerül felső klienshez csatlakoznia kkor megptóbál a testvéréhez, 
    //              de meg kell egyezniük ki csaltakozik kihez DONE
    //TODO ## túl hosszú draw line darabolása
    //TODO #7 multi canvas DONE
    //TODO #8 chat
    //TODO #9 PINGING DONE
    //TODO ## checkold le, hogy létrehozok e commandokat a canvas controlleren kívül DONE
    //TODO ## át kell írni a handhake szinkronizációt tab kompatibilisre DONE
    //TODO ## új signalok kellenek a tab vezérléshez DONE
    //TODO ## sznkornizációkor csak azokat a mementókat tároljuk el, és azokat a tabokat nyitjuk meg amikkel még nem rendelkezünk
    //TODO ## sinkronizációs signal létrehozása a körkörös szonkornizáció elkerülésére
    //TODO ## megfelelően lockolni kell nofity al a még nem szikronizált ConnectedClientEntity-k kimenetét

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

