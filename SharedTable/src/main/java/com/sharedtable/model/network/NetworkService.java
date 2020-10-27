package com.sharedtable.model.network;

import com.sharedtable.Constants;
import com.sharedtable.controller.*;
import com.sharedtable.controller.commands.Command;
import com.sharedtable.controller.commands.DrawImageCommand;
import com.sharedtable.model.network.UPnP.UPnPConfigException;
import com.sharedtable.model.network.UPnP.UPnPHandler;
import com.sharedtable.model.network.signals.*;
import com.sharedtable.controller.MainViewController;
import com.sharedtable.view.MessageBox;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public class NetworkService {

    private boolean timeToStop = false;
    private boolean reconnecting = false;
    private NetworkClientEntity me;
    private NetworkClientEntityTree entityTree;
    private ConnectedClientEntity upperConnectedClientEntity = null;
    private CopyOnWriteArrayList<ConnectedClientEntity> lowerConnectedClientEntities = new CopyOnWriteArrayList<>();
    private ConnectionReceiverThread connectionReceiverThread;
    private CopyOnWriteArrayList<NotifyableClientEntityTreeChange> clientEntityTreeChangeNotifyables = new CopyOnWriteArrayList<>();
    private String networkPassword = Constants.getNoPasswordConstant();
    private Logger logger = Logger.getLogger(MainViewController.class.getName());
    private static NetworkService instance = new NetworkService();
    private boolean isTesting;

    private NetworkService() {
    }

    public static NetworkService getInstance() {
        if(instance == null) {
            instance = new NetworkService();
        }
        return instance;
    }

    public void initLogger() {
        logger = Logger.getLogger(MainViewController.class.getName());
    }

    public void initService() {
        initLogger();
        me = new NetworkClientEntity(UserID.getInstance().getUserID(), UserID.getInstance().getNickname(), UserID.getInstance().getPublicIP(),
                -1,
                TabController.getInstance().getMementoCountOnAllCanvasController(),
                null, Constants.getBuildNumber());
        entityTree = new NetworkClientEntityTree(me);
    }

    public NetworkClientEntityTree getEntityTree() {
        return entityTree;
    }

    public boolean enableReceivingConnections(int port) {
        if (connectionReceiverThread != null) {
            MessageBox.showInformation("Már készen állok...", "A bejövő kapcsolatok fogadása már lehetséges!");
            return true;
        }
        int port1;
        if (port == -1) {
            port1 = 23243;
        } else {
            port1 = port;
        }

        try {
            prepareReceivingConnections(port1);
        } catch (IOException e) {
            logger.warning("port open failed! "+port1);
            enableReceivingConnections(port1 + 2);
             /*catch (IOException e1) {
                logger.severe("can't open any port!");
                MessageBox.showError("Hiba a port megnyitáskor!", "A rendszer nem engedélyezi port megnyitását ");
                e1.printStackTrace();
                return false;
            } */
        } catch (UPnPConfigException e) {
            logger.info("UPnP not supported!");
            showUPnPErrorMessage(port1);
            e.printStackTrace();
            return true;
        }
        return true;
    }


    private void showUPnPErrorMessage(Integer port1) {
        MessageBox.showError("UPnP konfigurálási hiba!",
                "Használjon port forwardingot \nerre a két potra: " + port1 + ", " + (port1 + 1));
    }

    //<editor-fold desc="CONNECTIVITY">
    //launch connection receiver thread
    private void prepareReceivingConnections(int port) throws IOException, UPnPConfigException {
        connectionReceiverThread = new ConnectionReceiverThread(port);
        connectionReceiverThread.start();
        me.setPort(connectionReceiverThread.getOpenedPort());
        logger.info("Prepared for receiving connections");
        UPnPHandler.getInstance().openPort(port);
        UPnPHandler.getInstance().openPort(port + 1); //byteReceiver
    }

    //make outgoing connection
    public void connect(final String IP, int port) throws IOException {
        if(isTesting) {
            return;
        }
        if (upperConnectedClientEntity != null) {
            reconnecting = true;
            upperConnectedClientEntity.handleScannerClose();
        }
        System.out.println("init socket1");
        Socket s1 = new Socket(IP, port);
        System.out.println("init socket2");
        Socket s2 = new Socket(IP, port + 1);
        upperConnectedClientEntity = new ConnectedClientEntity(s1, s2,
                false);
        reconnecting = false;
        //entityTree.addNetworkClientEntity(upperConnectedClientEntity.getNetworkClientEntity());
        //THREADING MIATT IDE MÁR SEMMI NEM JÖHET!

    }

    public boolean isPortOpened() {
        return me.getPort() != -1;
    }

    public int getOpenedPort() {
        return me.getPort();
    }

    public void subscribeForClientEntityTreeChange(NotifyableClientEntityTreeChange input) {
        clientEntityTreeChangeNotifyables.add(input);
    }

    public void unSubscribeForClientEntityTreeChange(NotifyableClientEntityTreeChange input) {
        clientEntityTreeChangeNotifyables.remove(input);
    }

    public void notifyClientEntityTreeChange() {
        logger.info("notifying subscribed controls about ClientEntityTreeChange...");
        for (var act : clientEntityTreeChangeNotifyables) {
            act.notifyClientEntityTreeChange();
        }
    }

    //</editor-fold> desc="CONNECTIVITY">

    //<editor-fold desc="MESSAGING">

    //sends all data to clients that connected to this client
    public void propagateCommandDownwards(Command command) {
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            try {
                act.sendCommand(command);
            } catch (Exception e) {
                logger.info("An error occured during sending commands downwards\n Command: " + command + "\nClient id: " + act.getUserId());
                throw new RuntimeException("An error occured during sending commands downwards");
            }
        }
    }

    public ChatMessageSignal sendNewChatMessage(String message) {
        ChatMessageSignal chatMessageSignal = new ChatMessageSignal(UserID.getInstance().getUserID(), UserID.getInstance().getNickname(), message);
        sendChatMessageSignal(chatMessageSignal);
        return chatMessageSignal;
    }

    //sends data to the client that this client connected
    public void propagateCommandUpwards(Command command) {
        if (upperConnectedClientEntity != null) {
            upperConnectedClientEntity.sendCommand(command);
        }
    }

    public void forwardMessageUpwards(String message) {
        if (upperConnectedClientEntity != null)
            upperConnectedClientEntity.sendPlainText(message);
    }

    public void forwardMessageDownwards(String message) {
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            act.sendPlainText(message);
        }
    }

    public void forwardMessageDownwardsWithException(String message, UUID except) {
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            if (!act.getUserId().equals(except))
                act.sendPlainText(message);
        }
    }

    public void forwardDrawImageCommandUpwards(DrawImageCommand command) {
        if (upperConnectedClientEntity != null)
            upperConnectedClientEntity.sendDrawImageCommand(command);
    }

    public void forwardDrawImageCommandDownwardsWithException(DrawImageCommand command, UUID except) {
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            if (!act.getUserId().equals(except))
                act.sendDrawImageCommand(command);
        }
    }

    public void forwardDrawImageCommandDownwards(DrawImageCommand command) {
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            act.sendDrawImageCommand(command);
        }
    }

    //</editor-fold> desc="MESSAGING">

    //<editor-fold desc="PINGING">

    private void prepareConnectedClientsToReceivePingResponse(UUID uuid) {
        if (upperConnectedClientEntity != null)
            upperConnectedClientEntity.setPingIDtoWaitFor(uuid);
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            act.setPingIDtoWaitFor(uuid);
        }
    }

    private long searchForResult(UUID id) {
        if (upperConnectedClientEntity != null &&
                upperConnectedClientEntity.getPingResult(id) != -1)
            return upperConnectedClientEntity.getPingResult(id);
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            if (act.getPingResult(id) != -1)
                return act.getPingResult(id);
        }
        return -1;
    }

    public long pingClient(UUID id) {
        long res = -1;
        PingSignal pingSignal = new PingSignal(UserID.getInstance().getUserID(), id, false);
        sendSignalUpwards(pingSignal);
        sendSignalDownwards(pingSignal);
        long start = System.nanoTime();
        prepareConnectedClientsToReceivePingResponse(pingSignal.getPingID());
        Sleep.sleep(1500, logger);
        long finish = searchForResult(pingSignal.getPingID());
        if (finish != -1) {
            res = finish - start;
        }
        logger.info("ping result: " + res + " ns (" + (res / (double) 1000000) + " ms)");
        return res;
    }

    //</editor-fold> desc="PINGING">

    //<editor-fold desc="RECONNECT">
    private NetworkClientEntity findMaxUUIDEntity(ArrayList<NetworkClientEntity> input) {
        NetworkClientEntity maxValueUUIDEntity = input.get(0);
        for(NetworkClientEntity act : input) {
            if(act.getID().compareTo(maxValueUUIDEntity.getID()) == 1) {
                maxValueUUIDEntity = act;
            }
        }
        return maxValueUUIDEntity;
    }

    private NetworkClientEntity chooseSiblingForNewUpper(ArrayList<NetworkClientEntity> siblings) {
        ArrayList<NetworkClientEntity> candidates = new ArrayList<>();
        for(NetworkClientEntity act : siblings) {
            if (act.getPort() != -1 &&
                    !act.getID().equals(UserID.getInstance().getUserID())) {
                candidates.add(act);
            }
        }
        if(candidates.isEmpty()) {
            return null;
        }
        return findMaxUUIDEntity(candidates);

    }

    private NetworkClientEntity connectToClientsChildrenForNewUpper(UUID id, boolean isSibling) {
        NetworkClientEntity entity = entityTree.getNetworkClientEntity(id);
        if(isSibling) {//saját testvérek közül kell választani
            logger.info("I have to choose from my siblings...");
            NetworkClientEntity newSiblingUpper = chooseSiblingForNewUpper(entityTree.getCloseChildren(entity));
            if(newSiblingUpper == null) {//nincs alkalmas kliens, vagy én vagyok az egyetlen alkalmas kliens
                logger.info("there is no valid sibling candidate to connect to or I'm the one");
                return null;
            }
            if(isPortOpened()){//ez a kliens alkalmas kapcsolatok fogadására-->dönteni kell ki fog kihez csatlakozni
                try {//alkalmas vagyok a kapcsolatok fogadására, de nem nekem van a legnagyobb UUID-m
                    logger.info("connectiong to sibling with grater UUID: "+newSiblingUpper.getID());
                    connect(newSiblingUpper.getIP(),newSiblingUpper.getPort());
                } catch (IOException e) {
                    logger.warning("cannot connect to the chosen sibling");
                    newSiblingUpper.setPort(-1);
                    connectToClientsChildrenForNewUpper(id,isSibling);
                    return null;
                }
                return newSiblingUpper;
            } else {//nem alkalmas kapcsolatok fogadására, így mindenképpen csatlakozó fél lesz
                try{
                    logger.info("I'm unable to receive incoming connections, so I'm going to connect to: "+newSiblingUpper.getID());
                    connect(newSiblingUpper.getIP(),newSiblingUpper.getPort());
                } catch (IOException e) {
                    logger.warning("cannot connect to the chosen sibling");
                    newSiblingUpper.setPort(-1);
                    connectToClientsChildrenForNewUpper(id,isSibling);
                    return null;
                }
                return newSiblingUpper;
            }
        }
        else {// nem saját testévrek közül kell választani
            logger.info("I don't have to choose from my siblings...");
            ArrayList<NetworkClientEntity> candidates = entityTree.getCloseChildren(entity);
            for(NetworkClientEntity act : candidates) {
                if (act.getPort() != -1) {
                    try{
                        connect(act.getIP(),act.getPort());
                    } catch (IOException e) {
                        act.setPort(-1);
                        continue;
                    }
                    return act;
                }
            }
            return null;
        }
    }

    //publicForTests
    private NetworkClientEntity findNewUpperClientEntityToConnect(UUID exUpperID) {
        //Sleep.sleep(1000);
        if (!entityTree.contains(exUpperID))
            return null;
        NetworkClientEntity exUpper = entityTree.getNetworkClientEntity(exUpperID);
        exUpper.setPort(-1);
        if (exUpper.getUpperClientID() != null) {//akkor neki van szülője
            NetworkClientEntity exUpperUpper = entityTree.getNetworkClientEntity(exUpper.getUpperClientID());
            logger.info("checking children of exUpperUpper...");
            NetworkClientEntity newUpper = connectToClientsChildrenForNewUpper(exUpperUpper.getID(), false);
            if (newUpper != null) {
                return newUpper;
            } else {
                logger.info("children check failed");
                logger.info("trying to connect to the parent of the children...");
                try {
                    connect(exUpperUpper.getIP(), exUpperUpper.getPort());
                } catch (IOException e) {
                    exUpperUpper.setPort(-1);
                    return findNewUpperClientEntityToConnect(exUpperUpper.getID());
                }
                return exUpperUpper;
            }
        } else {//akkor ő root
            NetworkClientEntity root = entityTree.getNetworkClientEntity(exUpperID);
            if(root.getID().equals(exUpperID)){
                return null;
            }
            logger.info("trying connect to root...");
            try {
                connect(root.getIP(), root.getPort());
                root.setPort(-1);
            } catch (IOException e) {
                return null;
            }
            return root;
        }
    }

    private NetworkClientEntity findNewSiblingClientEntityToConnect(UUID exUpperID) {
        //Sleep.sleep(1000);
        if (!entityTree.contains(exUpperID))
            return null;
        logger.info("try to connect to sibling");
        NetworkClientEntity newUpper = connectToClientsChildrenForNewUpper(exUpperID, true);
        if (newUpper != null) {
            return newUpper;
        } else {
            return null;
        }
    }


    //</editor-fold> desc="RECONNECT">

    //<editor-fold desc="SIGNAL HANDLING">

    public void handleChatMessageSignal(ChatMessageSignal signal) {
        ChatService.getInstance().handleIncomingChatMessageSignal(signal);
    }

    public void handleDiscoverySignal(DiscoverySignal discoverySignal) {
        NetworkClientEntity me = getMyNetworkClientEntity();
        forwardMessageUpwards(new NewClientSignal(me.getID(),
                me.getNickname(),
                me.getIP(),
                me.getPort(),
                me.getMementoNumber(),
                me.getUpperClientID(),
                me.getClientBuildNumber()).toString());
        forwardMessageDownwards(discoverySignal.toString());
    }

    public void sendNewTabSignal(UUID creatorID, UUID canvasID, String tabName) {
        Signal newTabSignal = new NewTabSignal(creatorID, canvasID, tabName);
        sendSignalDownwards(newTabSignal);
        sendSignalUpwards(newTabSignal);
    }

    public void sendChatMessageSignal(ChatMessageSignal signal) {
        sendSignalUpwards(signal);
        sendSignalDownwards(signal);
    }

    public void sendNetworkPasswordChangeSignal(UUID userID, String password) {
        Signal networkPasswordChangeSignal = new NetworkPasswordChangeSignal(userID, password);
        sendSignalUpwards(networkPasswordChangeSignal);
        sendSignalDownwards(networkPasswordChangeSignal);
    }

    public void sendCloseTabSignal(UUID creatorID, UUID canvasID) {
        Signal closeTabSignal = new CloseTabSignal(creatorID, canvasID);
        sendSignalDownwards(closeTabSignal);
        sendSignalUpwards(closeTabSignal);
    }

    public void sendRenameTabSignal(UUID creatorID, UUID canvasID, String newName) {
        Signal renameTabSignal = new RenameTabSignal(creatorID, canvasID, newName);
        sendSignalDownwards(renameTabSignal);
        sendSignalUpwards(renameTabSignal);
    }


    public void sendMementoOpenerSignal(UUID userID, UUID canvasID) {
        sendSignalUpwards(new MementoOpenerSignal(userID, canvasID));
        sendSignalDownwards(new MementoOpenerSignal(userID, canvasID));
    }

    public void sendMementoCloserSignal(UUID userID, UUID canvasID, UUID mementoID, boolean isLinked,
                                        UUID prevMementoID, UUID nextMementoID) {
        sendSignalUpwards(new MementoCloserSignal(userID, canvasID, mementoID, isLinked,prevMementoID,nextMementoID));
        sendSignalDownwards(new MementoCloserSignal(userID, canvasID, mementoID, isLinked, prevMementoID,nextMementoID));
    }

    public void sendDeleteAfterSignal(UUID userID, UUID canvasID, UUID mementoID) {
        sendSignalUpwards(new DeleteAfterSignal(userID, mementoID, canvasID));
        sendSignalDownwards(new DeleteAfterSignal(userID, mementoID, canvasID));
    }

    public void sendSignalUpwards(Signal signal) {
        forwardMessageUpwards(signal.toString());
    }

    public void sendSignalDownwards(Signal signal) {
        forwardMessageDownwards(signal.toString());
    }

    public void handleNetworkClientEntityTreeSignal(EntityTreeSignal signal) {
        if (amiRoot()) {
            return;
        } else {
            entityTree = signal.getEntityTree();
            notifyClientEntityTreeChange();
        }
    }

    public void sendEntityTreeSignal() {
        EntityTreeSignal signal = new EntityTreeSignal(UserID.getInstance().getUserID(), entityTree);
        sendSignalDownwards(signal);
    }

    public void sendDiscoverySignal() {
        if (!amiRoot())
            return;
        entityTree = new NetworkClientEntityTree(getMyNetworkClientEntity());

        DiscoverySignal signal = new DiscoverySignal(UserID.getInstance().getUserID());
        logger.info("sending discovery signal downwards... " + signal);
        sendSignalDownwards(signal);
    }

    public void handleNewClientSignal(NewClientSignal signal) {
        //forwardMessageUpwards(signal.toString());
        if (!amiRoot())
            return;
        NetworkClientEntity entity = new NetworkClientEntity(
                signal.getClientID(),
                signal.getNickname(),
                signal.getIP(),
                signal.getPort(),
                signal.getMementoNumber(),
                signal.getParentID(),
                signal.getClientBuildNumber());
        addNetworkClientEntity(entity);
        sendEntityTreeSignal();
        notifyClientEntityTreeChange();

    }

    public void handleDisconnectSignal(DisconnectSignal signal) {
        if (!amiRoot())
            return;
        sendDiscoverySignal();
    }

    //</editor-fold> desc="SIGNAL HANDLING">

    //<editor-fold desc="CLIENT OBJECT MANAGEMENT">

    public void setUpperClientEntity(NetworkClientEntity entity) {
        me.setUpperClientID(entity.getID());
    }

    public NetworkClientEntity getMyNetworkClientEntity() {
        me.setMementoNumber(TabController.getInstance().getMementoCountOnAllCanvasController());
        return me;
    }

    public void addNetworkClientEntity(NetworkClientEntity networkClientEntity) {
        if (amiRoot()) {
            entityTree.addNetworkClientEntity(networkClientEntity);

        }
        notifyClientEntityTreeChange();
    }

    private void logEntityTree() {
        StringBuilder sb = new StringBuilder();
        sb.append("All Network client entities: \n");
        for (NetworkClientEntity act : entityTree.getAllClients()) {
            sb.append(act).append("\n");
        }
        logger.info(sb.toString());
    }

    public synchronized void removeClientEntity(UUID id) {
        if (timeToStop)
            return;
        logger.info("try to remove becouse of disconnect: " + id.toString());
        if (upperConnectedClientEntity != null && upperConnectedClientEntity.getUserId().equals(id)) {
            upperConnectedClientEntity = null;
            logger.info("connection upwards has dropped. Trying to connect another client on the known network...");
            logEntityTree();
            if (!reconnecting && findNewUpperClientEntityToConnect(id) == null &&
                    findNewSiblingClientEntityToConnect(id) == null) {
                upperConnectedClientEntity = null;
                logger.info("Client search failed! I'm the new root!");
                entityTree.setMeRoot();
                sendDiscoverySignal();
                notifyClientEntityTreeChange();
            }
            return;
        }
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            if (act.getUserId().equals(id)) {
                lowerConnectedClientEntities.remove(act);
                if (amiRoot()) {
                    if(isTesting)
                        return;
                    sendDiscoverySignal();
                    notifyClientEntityTreeChange();
                }
                return;
            }
        }
    }

    public synchronized void addReceivedConnection(Socket connection, Socket brSocket) {
        ConnectedClientEntity connectedClientEntity = new ConnectedClientEntity(connection, brSocket,
                true);
        connectedClientEntity.setLowerClientEntity(true);
        lowerConnectedClientEntities.add(connectedClientEntity);
        logger.info("new lower ConnectedClientEntity added");
    }

    public boolean isClientInNetwork(UUID clientID) {
        return entityTree.contains(clientID);
    }

    public void timeToStop() {
        logger.info("stopping...");
        timeToStop = true;
        if (connectionReceiverThread != null)
            connectionReceiverThread.timeToStop();
        if (upperConnectedClientEntity != null)
            upperConnectedClientEntity.timeToStop();
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            act.timeToStop();
        }
        //semaphore.release();
    }

    public String getNetworkPassword() {
        return networkPassword;
    }

    public void setNetworkPassword(String password) {
        logger.info("changin password");
        networkPassword = password;
    }


    private ConnectedClientEntity getConnectedClientEntityByUUID(UUID uuid) {
        if (upperConnectedClientEntity != null && upperConnectedClientEntity.getUserId().equals(uuid)) {
            return upperConnectedClientEntity;
        }
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            if (act.getUserId().equals(uuid)) {
                return act;
            }
        }
        logger.info("getConnectedClientEntityByUUID(UUID uuid) User not found by ID");
        throw new RuntimeException("User not found by ID");
    }

    public boolean amiRoot() {
        return upperConnectedClientEntity == null;
    }

    public void switchToTestMode() {isTesting = true;}


    //</editor-fold> desc="CLIENT OBJECT MANAGEMENT">


}



