package com.sharedtable.model.Network;

import com.sharedtable.Constants;
import com.sharedtable.LoggerConfig;
import com.sharedtable.controller.*;
import com.sharedtable.controller.commands.Command;
import com.sharedtable.controller.commands.DrawImageCommand;
import com.sharedtable.model.Network.UPnP.UPnPConfigException;
import com.sharedtable.model.signals.*;
import com.sharedtable.view.MainView;
import com.sharedtable.view.MessageBox;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public class NetworkService {

    private NetworkService() {
    }

    public static void initLogger() {
        logger = LoggerConfig.setLogger(Logger.getLogger(MainView.class.getName()));
    }

    public static NetworkClientEntityTree getEntityTree() {
        return entityTree;
    }

    public static boolean enableReceivingConnections() {
        int port1 = 23243;
        try{
            prepareReceivingConnections(port1);
        } catch (IOException e) {
            try{
                logger.warning("standard port open failed!");
                prepareReceivingConnections(0);
            } catch (IOException e1){
                logger.severe("can't open any port!");
                MessageBox.showError("Hiba a port megnyitáskor!","A rendszer nem engedélyezi port megnyitását ");
                e1.printStackTrace();
                return false;
            } catch (UPnPConfigException e2) {
                showUPnPErrorMessage(port1);
                return false;
            }
        } catch (UPnPConfigException e) {
            logger.info("UPnP not supported!");
            showUPnPErrorMessage(port1);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void showUPnPErrorMessage(int port1) {
        MessageBox.showError("UPnP konfigurálási hiba!",
                "A routeren nem engedélyezett vagy támogatott\n az UPnP protkoll. " +
                        "Használjon port forwardingot \nerre a potra: "+port1);
    }

    //<editor-fold desc="CONNECTIVITY">
    //launch connection receiver thread
    private static void prepareReceivingConnections(int port) throws IOException, UPnPConfigException {
        connectionReceiverThread = new ConnectionReceiverThread(port);

        connectionReceiverThread.start();
        me.setPort(connectionReceiverThread.getOpenedPort());
        logger.info("Prepared for receiving connections");
    }

    //make outgoing connection
    public static void connect(final String IP, int port) throws IOException {
        upperConnectedClientEntity = new ConnectedClientEntity(new Socket(IP, port),
                false);

        //entityTree.addNetworkClientEntity(upperConnectedClientEntity.getNetworkClientEntity());
        //THREADING MIATT IDE MÁR SEMMI NEM JÖHET!

    }

    public static boolean isPortOpened() {
        return me.getPort() != -1;
    }

    public static int getOpenedPort() {
        return me.getPort();
    }

    public static void subscribeForClientEntityTreeChange(NotifyableClientEntityTreeChange input) {
        ClientEntityTreeChangeNotifyables.add(input);
    }

    public static void unSubscribeForClientEntityTreeChange(NotifyableClientEntityTreeChange input) {
        ClientEntityTreeChangeNotifyables.remove(input);
    }

    public static void notifyClientEntityTreeChange() {
        logger.info("notifying subscribed controls about ClientEntityTreeChange...");
        for(var act : ClientEntityTreeChangeNotifyables) {
            act.notifyClientEntityTreeChange();
        }
    }

    //</editor-fold> desc="CONNECTIVITY">

    //<editor-fold desc="MESSAGING">

    //sends all data to clients that connected to this client
    public static void propagateCommandDownwards(Command command) {
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            try {
                act.sendCommand(command);
            } catch (Exception e) {
                logger.info("An error occured during sending commands downwards\n Command: "+command+"\nClient id: "+act.getUserId());
                throw new RuntimeException("An error occured during sending commands downwards");
            }
        }
    }

    public static ChatMessageSignal sendNewChatMessage(String message) {
        ChatMessageSignal chatMessageSignal = new ChatMessageSignal(UserID.getUserID(),UserID.getNickname(),message);
        sendChatMessageSignal(chatMessageSignal);
        return chatMessageSignal;
    }

    //sends data to the client that this client connected
    public static void propagateCommandUpwards(Command command) {
        if (upperConnectedClientEntity != null) {
            upperConnectedClientEntity.sendCommand(command);
        }
    }

    public static void forwardMessageUpwards(String message) {
        if (upperConnectedClientEntity != null)
            upperConnectedClientEntity.sendPlainText(message);
    }

    public static void forwardMessageDownwards(String message) {
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            act.sendPlainText(message);
        }
    }

    public static void forwardMessageDownwardsWithException(String message, UUID except) {
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            if (!act.getUserId().equals(except))
                act.sendPlainText(message);
        }
    }

    public static void forwardDrawImageCommandUpwards(DrawImageCommand command) {
        if (upperConnectedClientEntity != null)
            upperConnectedClientEntity.sendDrawImageCommand(command);
    }

    public static void forwardDrawImageCommandDownwardsWithException(DrawImageCommand command, UUID except) {
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            if (!act.getUserId().equals(except))
                act.sendDrawImageCommand(command);
        }
    }

    public static void forwardDrawImageCommandDownwards(DrawImageCommand command) {
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            act.sendDrawImageCommand(command);
        }
    }

    public static void sendMessageToClient(UUID uuid, String message) {
        ConnectedClientEntity connectedClientEntity = getConnectedClientEntityByUUID(uuid);
        connectedClientEntity.sendPlainText(message);
    }


    //</editor-fold> desc="MESSAGING">

    //<editor-fold desc="PINGING">

    private static void prepareConnectedClientsToReceivePingResponse(UUID uuid) {
        if(upperConnectedClientEntity != null)
            upperConnectedClientEntity.setPingIDtoWaitFor(uuid);
        for(ConnectedClientEntity act : lowerConnectedClientEntities){
            act.setPingIDtoWaitFor(uuid);
        }
    }

    private static long searchForResult(UUID id) {
        if(upperConnectedClientEntity != null &&
                upperConnectedClientEntity.getPingResult(id)!=-1)
            return upperConnectedClientEntity.getPingResult(id);
        for(ConnectedClientEntity act : lowerConnectedClientEntities){
            if(act.getPingResult(id) != -1)
                return act.getPingResult(id);
        }
        return -1;
    }

    public static long pingClient(UUID id) {
        long res = -1;
        PingSignal pingSignal = new PingSignal(UserID.getUserID(),id,false);
        sendSignalUpwards(pingSignal);
        sendSignalDownwards(pingSignal);
        long start = System.nanoTime();
        prepareConnectedClientsToReceivePingResponse(pingSignal.getPingID());
        Sleep.sleep(1500,logger);
        long finish = searchForResult(pingSignal.getPingID());
        if(finish != -1){
             res = finish-start;
        }
        logger.info("ping result: "+res+" ns ("+(res/(double)1000000)+" ms)");
        return res;
    }

    //</editor-fold> desc="PINGING">

    //<editor-fold desc="RECONNECT">


    public static NetworkClientEntity checkClientChildrenForNewUpperToConnect(UUID id, boolean isSibling) {

        NetworkClientEntity entity = entityTree.getNetworkClientEntity(id);
        for(NetworkClientEntity act : entityTree.getCloseChildren(entity)) {
            if(act.getPort() != -1 && !act.getID().equals(UserID.getUserID())) {
                try {
                    if(!isSibling)
                        connect(act.getIP(), act.getPort());
                    else {
                        if(isPortOpened()) {
                            logger.info("both siblings are enabled to receive connections");
                            // hogy csak az egyik testvér kapcsolódjon a másikhoz, ha mindketten képesek kapcsolatot fogadni
                            if(UserID.getUserID().compareTo(act.getID()) == -1){
                                connect(act.getIP(), act.getPort());
                                logger.info("both siblings enabled to receive connections. This sibling connecting");
                            }
                        } else {
                            connect(act.getIP(), act.getPort());
                            logger.info("sibling found to connect to!");
                        }
                    }

                } catch (IOException e) {
                    continue;
                }
                return act;
            }
        }
        return null;
    }

    public static NetworkClientEntity findNewUpperClientEntityToConnect(UUID exUpperID) {
        //Sleep.sleep(1000);
        if(!entityTree.contains(exUpperID))
            return null;
        NetworkClientEntity exUpper = entityTree.getNetworkClientEntity(exUpperID);
        if(exUpper.getUpperClientID() != null) {
            NetworkClientEntity exUpperUpper = entityTree.getNetworkClientEntity(exUpper.getUpperClientID());
            logger.info("checking children of exUpperUpper...");
            NetworkClientEntity newUpper = checkClientChildrenForNewUpperToConnect(exUpperUpper.getID(),false);
            if(newUpper != null) {
                return newUpper;
            } else {
                logger.info("children check failed");
                logger.info("trying to connect to the parent of the children...");
                try {
                    connect(exUpperUpper.getIP(), exUpperUpper.getPort());
                } catch (IOException e) {
                    return findNewUpperClientEntityToConnect(exUpperUpper.getID());
                }
                return exUpperUpper;
            }
        } else {//akkor ő root
            logger.info("trying connect to root...");
            NetworkClientEntity root =  entityTree.getNetworkClientEntity(exUpperID);
            try {
                connect(root.getIP(), root.getPort());
            } catch (IOException e) {
                return null;
            }
            return root;
        }
    }

    public static NetworkClientEntity findNewSiblingClientEntityToConnect(UUID exUpperID) {
        //Sleep.sleep(1000);
        if(!entityTree.contains(exUpperID))
            return null;
        logger.info("try to connect to sibling");
        NetworkClientEntity newUpper = checkClientChildrenForNewUpperToConnect(exUpperID,true);
        if (newUpper != null) {
            return newUpper;
        } else {
            return null;
        }
    }

    //</editor-fold> desc="RECONNECT">

    //<editor-fold desc="SIGNAL HANDLING">

    public static void handleChatMessageSignal(ChatMessageSignal signal) {
        ChatService.handleIncomingChatMessageSignal(signal);
    }

    public static void handleDiscoverySignal(DiscoverySignal discoverySignal) {
        NetworkClientEntity me = NetworkService.getMyNetworkClientEntity();
        forwardMessageUpwards(new NewClientSignal(me.getID(),
                me.getNickname(),
                me.getIP(),
                me.getPort(),
                me.getMementoNumber(),
                me.getUpperClientID()).toString());
        forwardMessageDownwards(discoverySignal.toString());
    }

    public static void sendNewTabSignal(UUID creatorID, UUID canvasID, String tabName) {
        Signal newTabSignal = new NewTabSignal(creatorID,canvasID,tabName);
        sendSignalDownwards(newTabSignal);
        sendSignalUpwards(newTabSignal);
    }

    public static void sendChatMessageSignal(ChatMessageSignal signal) {
        sendSignalUpwards(signal);
        sendSignalDownwards(signal);
    }

    public static void sendNetworkPasswordChangeSignal(UUID userID, String password) {
        Signal networkPasswordChangeSignal = new NetworkPasswordChangeSignal(userID,password);
        sendSignalUpwards(networkPasswordChangeSignal);
        sendSignalDownwards(networkPasswordChangeSignal);
    }

    public static void sendCloseTabSignal(UUID creatorID, UUID canvasID) {
        Signal closeTabSignal = new CloseTabSignal(creatorID, canvasID);
        sendSignalDownwards(closeTabSignal);
        sendSignalUpwards(closeTabSignal);
    }

    public static void sendRenameTabSignal(UUID creatorID, UUID canvasID, String newName){
        Signal renameTabSignal = new RenameTabSignal(creatorID,canvasID,newName);
        sendSignalDownwards(renameTabSignal);
        sendSignalUpwards(renameTabSignal);
    }


    public static void sendMementoOpenerSignal(UUID userID, UUID canvasID, UUID mementoID, boolean isLinked) {
        sendSignalUpwards(new MementoOpenerSignal(userID, canvasID, mementoID, isLinked));
        sendSignalDownwards(new MementoOpenerSignal(userID,canvasID, mementoID, isLinked));
    }

    public static void sendMementoCloserSignal(UUID userID, UUID canvasID, UUID mementoID, boolean isLinked) {
        sendSignalUpwards(new MementoCloserSignal(userID, canvasID, mementoID, isLinked));
        sendSignalDownwards(new MementoCloserSignal(userID, canvasID, mementoID, isLinked));
    }

    public static void sendSignalUpwards(Signal signal) {
        forwardMessageUpwards(signal.toString());
    }

    public static void sendSignalDownwards(Signal signal) {
        forwardMessageDownwards(signal.toString());
    }

    public static void handleNetworkClientEntityTreeSignal(EntityTreeSignal signal) {
        if(amiRoot())
        { return; }
        else {
            entityTree = signal.getEntityTree();
            notifyClientEntityTreeChange();
        }
    }

    public static void sendEntityTreeSignal() {
        EntityTreeSignal signal = new EntityTreeSignal(UserID.getUserID(),entityTree);
        sendSignalDownwards(signal);
    }

    public static void sendDiscoverySignal() {
        if(!amiRoot())
            return;
        entityTree = new NetworkClientEntityTree(getMyNetworkClientEntity());

        DiscoverySignal signal = new DiscoverySignal(UserID.getUserID());
        logger.info("sending discovery signal downwards... "+signal);
        sendSignalDownwards(signal);
    }

    public static void handleNewClientSignal(NewClientSignal signal) {
        //forwardMessageUpwards(signal.toString());
        if(!amiRoot())
            return;
        NetworkClientEntity entity = new NetworkClientEntity(
                signal.getClientID(),
                signal.getNickname(),
                signal.getIP(),
                signal.getPort(),
                signal.getMementoNumber(),
                signal.getParentID());
        addNetworkClientEntity(entity);
        sendEntityTreeSignal();
        notifyClientEntityTreeChange();

    }

    public static void handleDisconnectSignal(DisconnectSignal signal) {
        if(!amiRoot())
            return;
        sendDiscoverySignal();
    }

    //</editor-fold> desc="SIGNAL HANDLING">

    //<editor-fold desc="CLIENT OBJECT MANAGEMENT">

    public static void setUpperClientEntity(NetworkClientEntity entity) {
        me.setUpperClientID(entity.getID());
    }


    public static NetworkClientEntity getMyNetworkClientEntity() {
        me.setMementoNumber(TabController.getMementoCountOnAllCanvasController());
        return me;
    }

    public static void addNetworkClientEntity(NetworkClientEntity networkClientEntity) {
        if(amiRoot()) {
            entityTree.addNetworkClientEntity(networkClientEntity);

        }
        notifyClientEntityTreeChange();
    }

    private static void logEntityTree() {
        StringBuilder sb = new StringBuilder();
        sb.append("All Network client entities: \n");
        for(NetworkClientEntity act : entityTree.getAllClients()) {
           sb.append(act).append("\n");
        }
        logger.info(sb.toString());
    }

    public static synchronized void removeClientEntity(UUID id) {
        if(timeToStop)
            return;
        logger.info("try to remove becouse of disconnect: "+id.toString());
        if(upperConnectedClientEntity != null && upperConnectedClientEntity.getUserId().equals(id)) {
            upperConnectedClientEntity = null;
            logger.info("connection upwards has dropped. Trying to connect another client on the known network...");
            logEntityTree();
            if(findNewUpperClientEntityToConnect(id) == null &&
                    findNewSiblingClientEntityToConnect(id) == null)
            {
                upperConnectedClientEntity = null;
                logger.info("Client search failed! I'm the new root!");
                entityTree.setMeRoot();
                sendDiscoverySignal();
                notifyClientEntityTreeChange();
            }
            return;
        }
        for(ConnectedClientEntity act : lowerConnectedClientEntities) {
            if(act.getUserId().equals(id)){
                lowerConnectedClientEntities.remove(act);
                if(amiRoot()){
                    sendDiscoverySignal();
                    notifyClientEntityTreeChange();
                }
                return;
            }
        }
    }

    public static synchronized void addReceivedConnection(Socket connection) {
            ConnectedClientEntity connectedClientEntity = new ConnectedClientEntity(connection,
                    true);
            connectedClientEntity.setLowerClientEntity(true);
            lowerConnectedClientEntities.add(connectedClientEntity);
            logger.info("new lower ConnectedClientEntity added");
    }

    public static boolean isClientInNetwork(UUID clientID) {
        return entityTree.contains(clientID);
    }

    public static void timeToStop() {
        logger.info("stopping...");
        timeToStop = true;
        if(connectionReceiverThread != null)
            connectionReceiverThread.timeToStop();
        if(upperConnectedClientEntity != null)
            upperConnectedClientEntity.timeToStop();
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            act.timeToStop();
        }
        //semaphore.release();
    }

    public static String getNetworkPassword() {
        return networkPassword;
    }

    public static void setNetworkPassword(String password) {
        logger.info("changin password");
        networkPassword = password;
    }


    private static ConnectedClientEntity getConnectedClientEntityByUUID(UUID uuid) {
        if(upperConnectedClientEntity != null && upperConnectedClientEntity.getUserId().equals(uuid)) {
            return upperConnectedClientEntity;
        }
        for(ConnectedClientEntity act : lowerConnectedClientEntities) {
            if(act.getUserId().equals(uuid)) {
                return act;
            }
        }
        logger.info("getConnectedClientEntityByUUID(UUID uuid) User not found by ID");
        throw new RuntimeException("User not found by ID");
    }

    public static boolean amiRoot() {return upperConnectedClientEntity == null;}
    //</editor-fold> desc="CLIENT OBJECT MANAGEMENT">

    private static boolean timeToStop = false;
    private static NetworkClientEntity me;
    private static NetworkClientEntityTree entityTree;
    private static ConnectedClientEntity upperConnectedClientEntity = null;
    private static CopyOnWriteArrayList<ConnectedClientEntity> lowerConnectedClientEntities = new CopyOnWriteArrayList<>();
    private static ConnectionReceiverThread connectionReceiverThread;
    private static CopyOnWriteArrayList<NotifyableClientEntityTreeChange> ClientEntityTreeChangeNotifyables = new CopyOnWriteArrayList<>();
    private static String networkPassword = Constants.getNoPasswordConstant();
    private static Logger logger = null;

    static {
        me = new NetworkClientEntity(UserID.getUserID(), UserID.getNickname(), UserID.getPublicIP(),
                -1,
                TabController.getMementoCountOnAllCanvasController(),
                null);
        entityTree = new NetworkClientEntityTree(me);
    }



}



