package com.sharedtable.model;

import com.sharedtable.controller.Command;
import com.sharedtable.controller.UserID;
import com.sharedtable.controller.controllers.CanvasController;
import com.sharedtable.model.signals.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class NetworkService {

    public NetworkService(boolean isServer, CanvasController canvasController, int port) {
        NetworkService.canvasController = canvasController;
        NetworkClientEntity me = new NetworkClientEntity(UserID.getUserID(), "nickname", getPublicIP(), port,
                canvasController.getMementos().size(), null);
        entityTree = new NetworkClientEntityTree(me);
        if (isServer) {
            prepareReceievingConnections(port);
        }
    }

    //<editor-fold desc="CONNECTIVITY">
    //launch connection receiver thread
    public void prepareReceievingConnections(int port) {
        connectionReceiverThread = new ConnectionReceiverThread(port);
        connectionReceiverThread.start();
        openedPort = port;
        System.out.println("Prepared for receiving connections");
    }

    //make outgoing connection
    public static void connect(final String IP, int port) throws IOException {
        upperConnectedClientEntity = new ConnectedClientEntity(new Socket(IP, port), canvasController, false);
        //entityTree.addNetworkClientEntity(upperConnectedClientEntity.getNetworkClientEntity());
        //THREADING MIATT IDE MÁR SEMMI NEM JÖHET!

        iamRoot = false;
    }

    public static boolean isPortOpened() {
        return openedPort != -1;
    }

    public static int getOpenedPort() {
        return openedPort;
    }

    public static String getPublicIP() {
        String systemipaddress = "";
        try {
            URL url_name = new URL("http://bot.whatismyipaddress.com");

            BufferedReader sc =
                    new BufferedReader(new InputStreamReader(url_name.openStream()));

            // reads system IPAddress
            systemipaddress = sc.readLine().trim();
        } catch (Exception e) {
            systemipaddress = "Cannot Execute Properly";
        }
        //return systemipaddress;
        return "127.0.0.1";
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

    //sends data to the client that this client connected
    public static void propagateCommandUpwards(Command command) {
        if (upperConnectedClientEntity != null) {
            upperConnectedClientEntity.sendCommand(command);
        }
    }

    public static void sendMementoOpenerSignal(UUID userID, UUID mementoID, boolean isLinked) {
        sendSignalUpwards(new MementoOpenerSignal(userID, mementoID, isLinked));
        sendSignalDownwards(new MementoOpenerSignal(userID, mementoID, isLinked));
    }

    public static void sendMementoCloserSignal(UUID userID, UUID mementoID, boolean isLinked) {
        sendSignalUpwards(new MementoCloserSignal(userID, mementoID, isLinked));
        sendSignalDownwards(new MementoCloserSignal(userID, mementoID, isLinked));
    }

    public static void sendSignalUpwards(Signal signal) {
        forwardMessageUpwards(signal.toString());
    }

    public static void sendSignalDownwards(Signal signal) {
        forwardMessageDownwards(signal.toString());
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

    public static void sendMessageToClient(UUID uuid, String message) {
        ConnectedClientEntity connectedClientEntity = getConnectedClientEntityByUUID(uuid);
        connectedClientEntity.sendPlainText(message);
    }

    /*public static void propagateAllClientInfo() {
        for (NetworkClientEntity act : entityTree.getAllClients()) {
            forwardMessageDownwardsWithException(new NewClientSignal(act.getID(),
                            act.getNickname(), act.getIP(), act.getPort(),
                            act.getMementoNumber(), act.getUpperClientID()).toString(),
                    act.getID());
            forwardMessageUpwards(new NewClientSignal(act.getID(),
                    act.getNickname(), act.getIP(), act.getPort(),
                    act.getMementoNumber(),
                    act.getUpperClientID()).toString());
        }
    }*/
    //</editor-fold> desc="MESSAGING">

    //<editor-fold desc="TreeHandling">

    public static void sendEntityTreeSignal() {
        EntityTreeSignal signal = new EntityTreeSignal(UserID.getUserID(),entityTree);
        sendSignalUpwards(signal);
        sendSignalDownwards(signal);
    }


    private static void imTheNewRoot() {
        iamRoot = true;
        //send root signal
    }

    //</editor-fold> desc="TreeHandling">

    //<editor-fold desc="CLIENT OBJECT MANAGEMENT">

    public static void handleNetworkClientEntityTreeSignal(EntityTreeSignal signal) {
        entityTree = signal.getEntityTree();
    }

    public static void addNetworkClientEntity(NetworkClientEntity networkClientEntity) {
        if(iamRoot)
            entityTree.addNetworkClientEntity(networkClientEntity);
    }

    public static void removeClientEntity(UUID id) {
        try { semaphore.acquire(); } catch (Exception e) {System.out.println(e);}

        if(upperConnectedClientEntity != null && upperConnectedClientEntity.getUserId().equals(id)) {
            upperConnectedClientEntity = null;
            entityTree.removeNetworkClientEntityWithException(
                    entityTree.getNetworkClientEntity(id),
                    entityTree.getNetworkClientEntity(UserID.getUserID())
            );
            imTheNewRoot();
            semaphore.release();
            return;
        }
        for(ConnectedClientEntity act : lowerConnectedClientEntities) {
            if(act.getUserId().equals(id)){
                lowerConnectedClientEntities.remove(act);
                if(iamRoot){
                    entityTree.removeNetworkClientEntity(act.getNetworkClientEntity());
                    sendEntityTreeSignal();
                }
                semaphore.release();
                return;
            }
        }
        semaphore.release();
    }

    public static boolean isClientInNetwork(UUID clientID) {
        return entityTree.contains(clientID);
    }

    public static NetworkClientEntity getMyHanshakingInfo() {
        return new NetworkClientEntity(UserID.getUserID(),"nickname",
                getPublicIP(),getOpenedPort(),canvasController.getMementos().size(),
                UserID.getUserID());
    }



    public static void addReceivedConnection(Socket connection) {
        try { semaphore.acquire(); } catch (Exception e) {System.out.println(e);}
        ConnectedClientEntity connectedClientEntity = new ConnectedClientEntity(connection, canvasController,true);
        connectedClientEntity.setLowerClientEntity(true);
        lowerConnectedClientEntities.add(connectedClientEntity);
        //addNetworkClientEntity(connectedClientEntity.getNetworkClientEntity());
        semaphore.release();
    }

    public static void timeToStop() {
        connectionReceiverThread.timeToStop();
        if(upperConnectedClientEntity != null)
            upperConnectedClientEntity.timeToStop();
        for (ConnectedClientEntity act : lowerConnectedClientEntities) {
            act.timeToStop();
        }
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

    public static void printClientList() {
        System.out.println("---------CLIENT LIST----------");
        for(NetworkClientEntity act : entityTree.getAllClients()) {
            System.out.println(act);
        }
        System.out.println("---------CLIENT LIST END----------");
    }

    public static boolean amiRoot() {return iamRoot;}
    //</editor-fold> desc="CLIENT OBJECT MANAGEMENT">

    private static boolean iamRoot = true;
    private static NetworkClientEntityTree entityTree;
    private static ConnectedClientEntity upperConnectedClientEntity = null;
    private static ArrayList<ConnectedClientEntity> lowerConnectedClientEntities = new ArrayList<>();
    private static int openedPort = -1;
    private static ConnectionReceiverThread connectionReceiverThread;
    private static CanvasController canvasController;
    private static Semaphore semaphore = new Semaphore(1);

    /*
    -ha alulról kap infót, azt felfele, és lefel is továbbküldi (értelemszerűen a forrásnak nem)
    -ha felülről jön akkor tovább küldi mindenkinek lefele
     */
}



    /*public static void sendMementoOpenerSignalToClient(UUID userID,UUID mementoID) {
        sendMessageToClient(userID,getMementoOpenerSignal(userID, mementoID));
    }

    public static void sendMementoCloserSignalToClient(UUID userID,UUID mementoID) {
        sendMessageToClient(userID,getMementoCloserSignal(userID, mementoID));
    }*/

    /*private static void reconnectToAnotherNetworkClient() {
        for(NetworkClientEntity act : allNetworkClients) {
            if(act.hasOpenedPort()){
                try {connect(act.getIP(),act.getPort());}
                catch (IOException e) {
                    System.out.println("reconnection failed with: "+act.getID());
                    continue;
                }
                System.out.println("reconnection succesfull with: "+act.getID());
                return;
            } else {
                System.out.println("doesn't have opened port: "+act.getID());
            }
        }
    }*/


     /*if(connectedClientEntity == null && lowerConnectedClientEntities.isEmpty() ) {
            reconnectToAnotherNetworkClient();
        }*/