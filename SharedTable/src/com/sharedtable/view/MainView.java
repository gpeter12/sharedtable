package com.sharedtable.view;


import com.sharedtable.Constants;
import com.sharedtable.LoggerConfig;
import com.sharedtable.controller.*;
import com.sharedtable.model.Network.NetworkService;
import com.sharedtable.model.Network.UPnP.UPnPConfigException;
import com.sharedtable.model.Network.UPnP.UPnPHandler;
import com.sharedtable.model.Persistence.UserDataPersistence;
import com.sharedtable.model.signals.NetworkPasswordChangeSignal;
import com.sharedtable.model.signals.Signal;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;


public class MainView extends Application  {

    @Override
    public void start(Stage primaryStageArg) {
        logger = LoggerConfig.setLogger(Logger.getLogger(MainView.class.getName()));
        logger.info("appstarted");

        NetworkService.initLogger();

        Scene scene = initMainView(primaryStageArg);
        initUserData();
        initControls(scene);
    }

    private Scene initMainView(Stage primaryStageArg) {
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("MainView.fxml"));
        } catch (IOException e) {
            logger.severe("failed to get resource MainView.fxml");
            return null;
        }
        primaryStage = primaryStageArg;
        primaryStage.setTitle("Shared Table (mode: "+startMode+")");
        Scene scene = new Scene(root, 640, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
        return scene;
    }

    private void initControls(Scene scene) {
        tabPane = (STTabPane)scene.lookup("#tabPane");
        new TabController(tabPane, primaryStage);


        colorPicker = (ColorPicker)scene.lookup("#colorPicker");
        colorPicker.setValue(Color.BLACK);
        setColorOnAllCanvases(Color.BLACK);


        lineWidthPicker = (ComboBox) scene.lookup("#lineWidthPicker");
        Object availableWidths[] =
                { "1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","22","24","26","28","32","36","40" };
        lineWidthPicker.getItems().addAll(availableWidths);

        tabPane.minHeightProperty().bind(primaryStage.heightProperty());
        tabPane.minWidthProperty().bind(primaryStage.widthProperty());
    }

    private void initUserData() {
        UserDataPersistence userDataPersistence = new UserDataPersistence();
        UserID.setPersistence(userDataPersistence);
        if(userDataPersistence.isRequiresInit()){
            logger.info("user data file not initalized yet");
            SetClientDataWindowController setClientDataWindowController =
                    (SetClientDataWindowController) new SetClientDataView().getController();
            UserID.setNickname(setClientDataWindowController.getNickname());
        }
    }

    @Override
    public void stop() {
        logger.info("stopping application");
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
        if(ClientsWindowView.isOpened())
            return;
        new ClientsWindowView(primaryStage);
    }

    @FXML
    public void onConnectPressed(ActionEvent actionEvent) {
        ConnectWindowController connectWindowController = (ConnectWindowController) new ConnectWindowView().getController();
        try {
            if(!connectWindowController.isCanceled()) {
                NetworkService.connect(connectWindowController.getConnectionLink().getIP(),
                        connectWindowController.getConnectionLink().getPort());
                if(connectWindowController.getPassword().isEmpty()){
                    NetworkService.setNetworkPassword(Constants.getNoPasswordConstant());
                } else {
                    NetworkService.setNetworkPassword(connectWindowController.getPassword());
                }
                Signal networkPasswordChangeSignal = new NetworkPasswordChangeSignal(UserID.getUserID(),connectWindowController.getPassword());
                NetworkService.sendSignalDownwards(networkPasswordChangeSignal);
            }
        } catch (IOException e) {
            logger.fine("connaction failed: "+e.getMessage());
            MessageBox.showError("Sikertelen kapcsolódás!","A megadott címre jelenleg nem lehet kapcsolódni.\n"+e.getMessage());
        }
    }

    @FXML
    public void onCreateNewTabPressed(ActionEvent actionEvent) {
        UUID tabID = UUID.randomUUID();
        RenameTabView renameTabView = new RenameTabView();
        RenameTabController renameTabController = ((RenameTabController) renameTabView.getController());
        if(!renameTabController.isCanceled()) {
            TabController.createNewTab(tabID, renameTabController.getTabName());
            NetworkService.sendNewTabSignal(UserID.getUserID(),tabID,renameTabController.getTabName());
        }

    }

    @FXML
    public void onTestConnectPressed(ActionEvent actionEvent) {
        try {
            NetworkService.connect(IP, 2222);
        } catch (IOException e) {
            logger.severe("failed to connect in startMode -2");
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

    @FXML
    public void onOpenChatPressed(ActionEvent actionEvent) {
        new ChatWindowView(primaryStage);
    }

    @FXML
    public void onDrawRectangleModePressed(ActionEvent actionEvent) {
        setDrawingModeOnAllCanvases(DrawingMode.Rectangle);
    }

    @FXML
    public void onDrawLineModePressed(ActionEvent actionEvent) {
        setDrawingModeOnAllCanvases(DrawingMode.ContinousLine);
    }

    @FXML
    public void onDrawTriangleModePressed(ActionEvent actionEvent) {
        setDrawingModeOnAllCanvases(DrawingMode.Triangle);
    }

    @FXML
    public void onDrawEllipseModePressed(ActionEvent actionEvent) {
        setDrawingModeOnAllCanvases(DrawingMode.Ellipse);
    }

    @FXML
    public void onDrawImageModePressed(ActionEvent actionEvent) {
        Image image = getImageFromClipboard();
        if(image != null){
            MessageBox.showInformation("Kép beolvasva! ","A bal egérgomb lenyomvatartásával, \nközben az egér mozgatásával jelölje \nki a kép helyét, és méreteit!");
            setDrawingModeOnAllCanvases(DrawingMode.Image);
            setImageOnAllCanvases(image);
        }
        else {
            logger.info("wrong image format on clipboard");
            MessageBox.showError("Hiba a kép beillesztésekor",
                    "nem megfelelő formátumú bellesztési tartalom");
        }
    }

    @FXML
    public void onChangeNetworkPasswordPressed(ActionEvent actionEvent) {
        ChangePasswordView changePasswordView = new ChangePasswordView();
        ChangePasswordWindowController changePasswordWindowController = (ChangePasswordWindowController) changePasswordView.getController();
        if(!changePasswordWindowController.isCanceled()) {
            NetworkService.setNetworkPassword(changePasswordWindowController.getPassword());
            NetworkService.sendNetworkPasswordChangeSignal(UserID.getUserID(),changePasswordWindowController.getPassword());
        }
    }

    @FXML
    public void onRenameCurrentTabPressed(ActionEvent actionEvent) {
        RenameTabView renameTabView = new RenameTabView();
        RenameTabController renameTabController = (RenameTabController)renameTabView.getController();
        TabController.renameTab(TabController.getActualCanvasControler().getCanvasID(),
                renameTabController.getTabName());
        NetworkService.sendRenameTabSignal(UserID.getUserID(),
                TabController.getActualCanvasControler().getCanvasID(),
                renameTabController.getTabName());
    }

    @FXML
    public void onEnableIncomingConnectionsPressed(ActionEvent actionEvent) {
        if(NetworkService.enableReceivingConnections())
            new ConnectionLinkView();
    }

    @FXML
    public void onEraserPressed(ActionEvent actionEvent) {
        setColorOnAllCanvases(Color.WHITE);
        colorPicker.setValue(Color.WHITE);
        lineWidthPicker.setValue(String.valueOf(16));
        setLineWidthOnAllCanvases(16);
    }


    private void setDrawingModeOnAllCanvases(DrawingMode drawingMode) {
        for(CanvasController act : TabController.getAllCanvasControllers()) {
            act.setDrawingMode(drawingMode);
        }
    }

    private void setColorOnAllCanvases(Color color) {
        for(CanvasController act : TabController.getAllCanvasControllers()) {
            act.setColor(color);
        }
    }

    private static void setLineWidthOnAllCanvases(int lineWidth) {
        for(CanvasController act : TabController.getAllCanvasControllers()) {
            act.setLineWidth(lineWidth);
        }
    }

    private void setImageOnAllCanvases(Image image) {
        for(CanvasController act : TabController.getAllCanvasControllers()) {
            act.setCurrentImage(image);
        }
    }

    public Image getImageFromClipboard()
    {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            BufferedImage image = (BufferedImage) clipboard.getData(DataFlavor.imageFlavor);
            return SwingFXUtils.toFXImage(image, null);
        } catch (UnsupportedFlavorException e) {
            logger.info(e.getMessage());
            return null;
        } catch (IOException e) {
            logger.info(e.getMessage());
            return null;
        }
    }

    private static void closeOpenedPortOnRouter() {
        if(UPnPHandler.getOpenedPort() == -1)
            return;
        logger.info("closing opened port on router...");
        try{
            UPnPHandler.closePort(UPnPHandler.getOpenedPort());
        }
        catch (UPnPConfigException e){
            logger.severe("UPnP.closePort() failed!");
        }
    }

    public static void main(String[] args) {
        startMode = Integer.parseInt(args[0]);
        if (args.length > 1) {
            IP = args[1];
            port = Integer.parseInt(args[2]);
        }
        try {
            launch(args);
            closeOpenedPortOnRouter();
        } catch (Exception e) {
            logger.severe("FATAL EXCEPTION bubbled up to Main() "+e.getMessage());
        } finally {
            closeOpenedPortOnRouter();
        }
    }

    @FXML
    private STTabPane tabPane;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private ComboBox lineWidthPicker;
    private static int startMode;
    private static String IP = null;
    private static int port = -1;
    private Stage primaryStage;
    private static Logger logger = null;


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
    //TODO ## minden hálózatban levő kliens folyamatos nyilvántartása NetworkClientEntityTree-ban (kell az átcsatlakozáshoz) DONE
    //TODO #6 ha egy csomópont kiszáll, megpróbál sorrendben csatlakozni bármely más klienshez DONE
    //TODO ## ha nem sikerül felső klienshez csatlakoznia kkor megptóbál a testvéréhez, 
    //              de meg kell egyezniük ki csaltakozik kihez DONE

    //TODO #7 multi canvas DONE
    //TODO #9 PINGING DONE
    //TODO ## checkold le, hogy létrehozok e commandokat a canvas controlleren kívül DONE
    //TODO ## át kell írni a handhake szinkronizációt tab kompatibilisre DONE
    //TODO ## új signalok kellenek a tab vezérléshez DONE
    //TODO ## sznkornizációkor csak azokat a mementókat tároljuk el, és azokat a tabokat nyitjuk meg amikkel még nem rendelkezünk DONE
    //TODO ## sinkronizációs signal létrehozása a körkörös szonkornizáció elkerülésére DONE
    //TODO ## megfelelően lockolni kell nofity al a még nem szikronizált ConnectedClientEntity-k kimenetét DONE
    //TODO ## visszavonásokhoz 350ms sleep DONE
    //TODO #8 chat DONE
    //TODO ## ne lehessen több chat window-t megnyitni DONE
    //TODO #7 ellipszis DONE
    //TODO #8 téglalap DONE
    //TODO #9 háromszög DONE



    //TODO ## értelmesen átméretezhetővé tenni az STCanvas-t DONE
    //TODO ## UserID initFromModel(Model) DONE
    //TODO ## scrollable chat flow DONE
    //TODO ## image paste with exception handling DONE
    //TODO ## UPnP beinplementálása DONE
    //TODO ## jelszavas védelem DONE
    //TODO #X a ConnectWindow-ra kiírni a stconnect linket, és a link mezőt. súgógombok a linkek mellé. DONE
    //TODO #X kiírni rögtön init után, ha nem lehet UPNP-n portot nyitni, és tájékoztatni a tűzfalról is DONE
    //---------LOW PRIORITY-------------------------
    //TODO ## túl vastag vonalnál a vonalakat ellipszisekből építjük


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

