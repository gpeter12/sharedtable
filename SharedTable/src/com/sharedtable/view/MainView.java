package com.sharedtable.view;


import com.sharedtable.Constants;
import com.sharedtable.controller.*;
import com.sharedtable.model.Network.NetworkService;
import com.sharedtable.model.Network.UPnP.UPnPConfigException;
import com.sharedtable.model.Network.UPnP.UPnPHandler;
import com.sharedtable.model.Persistence.FilePathHandler;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class MainView extends Application  {

    @Override
    public void start(Stage primaryStageArg) {
        logger = setLogger(Logger.getLogger(MainView.class.getName()));
        NetworkService.initLogger();

        Scene scene = initMainView(primaryStageArg);
        if(isInDebugMode) {
            initDebugMode(startMode);
        } else {
            initUserData();
        }
        viewAvailableUpdates();
        initControls(scene);
        NetworkService.initService();
    }

    private void viewAvailableUpdates() {
        UpdateChecker updateChecker = new UpdateChecker("http://gpeter12.web.elte.hu/stBuildNum");
        if(updateChecker.isUpdateAvailable()){
            logger.info("new update available!");
            new UpdateNotificationView();
        }

    }

    private Scene initMainView(Stage primaryStageArg) {
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("MainView.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("failed to get resource MainView.fxml "+e.getMessage());
            return null;
        }
        primaryStage = primaryStageArg;
        primaryStage.setTitle("Shared Table");
        Scene scene = new Scene(root, 1366, 768);
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
        UserID.setPublicIP(getExternalIP());
        UserDataPersistence userDataPersistence = new UserDataPersistence();
        UserID.initWithPersistence(userDataPersistence);
        if(userDataPersistence.isRequiresInit()){
            logger.info("user data file not initalized yet");
            SetClientDataWindowController setClientDataWindowController =
                    (SetClientDataWindowController) new SetClientDataView().getController();
            UserID.setNickname(setClientDataWindowController.getNickname());
        }
    }

    private void initDebugMode(int mode) {
        primaryStage.setTitle("Debug mode: "+mode);
        logger.info("application staarted in debug mode: "+mode);
        UserID.setUserID(UUID.randomUUID());
        NetworkService.initService();
        UserID.setUserID(UUID.randomUUID());
        if (startMode == 3) {
            NetworkService.enableReceivingConnections(3334);
        } else if (startMode == 2) {
            NetworkService.enableReceivingConnections(2223);
            try {
                NetworkService.connect(IP, 3334);
            } catch (IOException e) {
                logger.severe("failed to connect in startMode 2");
            }
        } else if (startMode == 1) {
            NetworkService.enableReceivingConnections(2324);
            try {
                NetworkService.connect(IP, 2223);
            } catch (IOException e) {
                logger.severe("failed to connect in startMode 1");
            }
        } else if (startMode == 0) {
            NetworkService.enableReceivingConnections(2225);
            try {
                NetworkService.connect(IP, 2324);
            } catch (IOException e) {
                logger.severe("failed to connect in startMode 0");
            }
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

    private Image getImageWithBrowsing() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(primaryStage);
        try{
            return new Image("file:"+file.getAbsolutePath());
        } catch (Exception e) {
            logger.warning("invalid image file path ("+file.getAbsolutePath()+")! "+e.getMessage());
            return null;
        }
    }

    @FXML
    public void onDrawImageFileModePressed(ActionEvent actionEvent) {
        Image image = getImageWithBrowsing();
        if(image != null){
            MessageBox.showInformation("Kép beolvasva! ","A bal egérgomb lenyomvatartásával, \nközben az egér mozgatásával jelölje \nki a kép helyét, és méreteit!");
            setDrawingModeOnAllCanvases(DrawingMode.Image);
            setImageOnAllCanvases(image);
        }
        else {
            logger.info("wrong image format in file");
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
        if(NetworkService.enableReceivingConnections(-1))
            new ConnectionLinkView();
    }

    @FXML
    public void onEraserPressed(ActionEvent actionEvent) {
        setDrawingModeOnAllCanvases(DrawingMode.ContinousLine);
        setColorOnAllCanvases(Color.WHITE);
        colorPicker.setValue(Color.WHITE);
        lineWidthPicker.setValue(String.valueOf(16));
        setLineWidthOnAllCanvases(16);
    }

    @FXML
    public void onOpenWebViewPressed(ActionEvent actionEvent) {
        new UpdateNotificationView();
    }

    @FXML
    public void onAboutPressed(ActionEvent actionEvent) {
        new AboutView();
    }

    @FXML
    public void onFindUpdatePressed(ActionEvent actionEvent) {
        UpdateChecker updateChecker = new UpdateChecker("http://gpeter12.web.elte.hu/stBuildNum");
        if(updateChecker.isUpdateAvailable()){
            viewAvailableUpdates();
        } else {
            MessageBox.showInformation("Nem érhető el frissítés!","Ez a kliens jelenleg a legfrissebb \nverzióval rendelkezik.");
        }
    }

    @FXML
    public void onHelpPressed(ActionEvent actionEvent) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI("http://gpeter12.web.elte.hu/sharedtable/faq.html"));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }).start();
        } else {
            MessageBox.showError("Nem nyitható meg böngésző!","Ez az OS nem támogatja \nböngésző ablak megnyitását");
        }
    }



    private void setDrawingModeOnAllCanvases(DrawingMode drawingMode) {
        setDefaultColor();
        setDefaultLineWidth();
        for(CanvasController act : TabController.getAllCanvasControllers()) {
            act.setDrawingMode(drawingMode);
        }
    }

    private void setDefaultColor() {
        setColorOnAllCanvases(Color.BLACK);
        colorPicker.setValue(Color.BLACK);
    }

    private void setDefaultLineWidth(){
        lineWidthPicker.setValue(String.valueOf(1));
        setLineWidthOnAllCanvases(1);
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
            UPnPHandler.closePort(UPnPHandler.getOpenedPort()+1);
        }
        catch (UPnPConfigException e){
            logger.severe("UPnP.closePort() failed!");
        }
    }

    private static String getPublicIPFromRouter() throws UPnPConfigException {
        return UPnPHandler.getExternalIP();
    }

    private static String getPublicIPFromWeb() throws ExternalIPDownloadFailureException {
        String systemipaddress = "";
        try {
            URL url_name = new URL("http://bot.whatismyipaddress.com");

            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));

            // reads system IPAddress
            systemipaddress = sc.readLine().trim();
        } catch (Exception e) {
            throw new ExternalIPDownloadFailureException(e.getMessage());
        }
        return systemipaddress;
    }

    private static String getExternalIP() {
        try{
            return getPublicIPFromRouter();
        } catch (UPnPConfigException e) {
            //e.printStackTrace();
            logger.warning("can't get External IP due to UPnPConfigException! Trying to get it from web...");
            try {
                return getPublicIPFromWeb();
            } catch (ExternalIPDownloadFailureException e2) {
                logger.warning("can't get external IP due to ExternalIPDownloadFailureException: "+e2);
                return "ExternalIPDownloadFailureException";
            }
        }
    }

    private static Logger setLogger(Logger logger){
        FileHandler fh=null;

        try {
            if (Constants.isPlatformWindows()) {
                FilePathHandler.createDirectory(FilePathHandler.getDirectoryPathOnWindows());
                fh = new FileHandler(FilePathHandler.getDirectoryPathOnWindows() + "\\logfile.log");
            } else if (Constants.isPlatformLinux()) {
                FilePathHandler.createDirectory(FilePathHandler.getDirectoryPathOnLinux());
                fh = new FileHandler(FilePathHandler.getDirectoryPathOnLinux() + "/logfile.log");
            }
        } catch (Exception e) {
            System.out.println("creating logfile error");
            MessageBox.showError("Hiba a naplófájl létrehozásakor!","");
        }

        logger.addHandler(fh);
        logger.setLevel(Level.FINEST);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        logger.getHandlers()[0].setLevel(Level.FINEST);

        return logger;
    }

    private static String getConfigPathFromArgs(String[] args) {
        for(int i=0; i<args.length; i++) {
            if(args[i].equals("--config-path")){
                return args[i+1];
            }
        }
        return null;
    }

    private static void processConfigPathArg(String[] args) {
        String configPath = getConfigPathFromArgs(args);
        if(configPath == null)
            return;
        FilePathHandler.setCustomConfigPath(configPath);
    }

    public static void main(String[] args) {

        processConfigPathArg(args);

        if(args.length > 1 && args[0].equals("debug")){
            isInDebugMode =true;
            startMode = Integer.parseInt(args[1]);
        }

        try {
            launch(args);
            closeOpenedPortOnRouter();
        } catch (Exception e) {
            e.printStackTrace();
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
    private static String IP = "127.0.0.1";
    private static int port = -1;
    private Stage primaryStage;
    private static Logger logger = Logger.getLogger(MainView.class.getName());
    private static boolean isInDebugMode = false;



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

