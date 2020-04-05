package com.sharedtable.model;

import com.sharedtable.controller.*;
import com.sharedtable.controller.commands.Command;
import com.sharedtable.model.signals.*;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

public class NetworkService {

    private NetworkService() { }

    public static NetworkClientEntityTree getEntityTree() {
        return entityTree;
    }



    //<editor-fold desc="CONNECTIVITY">
    //launch connection receiver thread
    public static void prepareReceievingConnections(int port) {
        connectionReceiverThread = new ConnectionReceiverThread(port);
        connectionReceiverThread.start();
        me.setPort(port);
        System.out.println("Prepared for receiving connections");
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
        System.out.println("subscribed");
        ClientEntityTreeChangeNotifyables.add(input);
    }

    public static void unSubscribeForClientEntityTreeChange(NotifyableClientEntityTreeChange input) {
        ClientEntityTreeChangeNotifyables.remove(input);
        System.out.println("unSubscribed");
    }

    public static void notifyClientEntityTreeChange() {
        System.out.println("notifying...");
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

    public static void forwardBytesUpwards(byte[] bytes) {
        if (upperConnectedClientEntity != null)
            upperConnectedClientEntity.sendByteArray(bytes);
    }

    public static void forwardBytesDownwardsWithException(byte[] bytes, UUID except) {
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            if (!act.getUserId().equals(except))
                act.sendByteArray(bytes);
        }
    }

    public static void forwardBytesDownwards(byte[] bytes) {
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            act.sendByteArray(bytes);
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
        Sleep.sleep(1500);
        long finish = searchForResult(pingSignal.getPingID());
        if(finish != -1){
             res = finish-start;
        }
        System.out.println("ping result: "+res+" ns ("+(res/(double)1000000)+" ms)");
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
                            // hogy csak az egyik testvér kapcsolódjon a másikhoz, ha mindketten képesek kapcsolatot fogadni
                            if(UserID.getUserID().compareTo(act.getID()) == -1){
                                connect(act.getIP(), act.getPort());
                                System.out.println("!!!!!!Im the one who conntects!!!!!");
                            }
                        } else {
                            connect(act.getIP(), act.getPort());
                            System.out.println("sibling found to connect to!");
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
        Sleep.sleep(1000);
        NetworkClientEntity exUpper = entityTree.getNetworkClientEntity(exUpperID);
        if(exUpper.getUpperClientID() != null) {
            NetworkClientEntity exUpperUpper = entityTree.getNetworkClientEntity(exUpper.getUpperClientID());
            System.out.println("checking children of exUpperUpper...");
            NetworkClientEntity newUpper = checkClientChildrenForNewUpperToConnect(exUpperUpper.getID(),false);
            if(newUpper != null) {
                return newUpper;
            } else {
                System.out.println("children check failed");
                System.out.println("trying to connect to the parent of the children...");
                try {
                    connect(exUpperUpper.getIP(), exUpperUpper.getPort());
                } catch (IOException e) {
                    return findNewUpperClientEntityToConnect(exUpperUpper.getID());
                }
                return exUpperUpper;
            }
        } else {//akkor ő root
            System.out.println("trying connect to root...");
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
        Sleep.sleep(1000);
        System.out.println("try to connect to sibling");
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

    public static void sendCloseTabSignal(UUID creatorID, UUID canvasID) {
        Signal closeTabSignal = new CloseTabSignal(creatorID, canvasID);
        sendSignalDownwards(closeTabSignal);
        sendSignalUpwards(closeTabSignal);
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
        //sendSignalDownwards(signal);

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

    public static synchronized void removeClientEntity(UUID id) {
        if(timeToStop)
            return;
        System.out.println("try to remove: "+id.toString());
        if(upperConnectedClientEntity != null && upperConnectedClientEntity.getUserId().equals(id)) {
            upperConnectedClientEntity = null;
            if(findNewUpperClientEntityToConnect(id) == null &&
                    findNewSiblingClientEntityToConnect(id) == null)
            {
                upperConnectedClientEntity = null;
                System.out.println("Im the new root!");
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
                    //entityTree.removeNetworkClientEntity(act.getNetworkClientEntity());
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
            //addNetworkClientEntity(connectedClientEntity.getNetworkClientEntity());
    }

    public static boolean isClientInNetwork(UUID clientID) {
        return entityTree.contains(clientID);
    }

    public static void timeToStop() {
        //acquireSemaphore();
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

    private static ConnectedClientEntity getConnectedClientEntityByUUID(UUID uuid) {
        if(upperConnectedClientEntity != null && upperConnectedClientEntity.getUserId().equals(uuid)) {
            return upperConnectedClientEntity;
        }
        for(ConnectedClientEntity act : lowerConnectedClientEntities) {
            if(act.getUserId().equals(uuid)) {
                return act;
            }
        }
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
    private static int reconnectTry = 0;
    private static CopyOnWriteArrayList<NotifyableClientEntityTreeChange> ClientEntityTreeChangeNotifyables = new CopyOnWriteArrayList<>();

    static {
        me = new NetworkClientEntity(UserID.getUserID(), UserID.getNickname(), UserID.getPublicIP(),
                -1,
                TabController.getMementoCountOnAllCanvasController(),
                null);
        entityTree = new NetworkClientEntityTree(me);
    }


}



