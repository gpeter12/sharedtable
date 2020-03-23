package com.sharedtable.model;

import com.sharedtable.controller.UserID;
import com.sharedtable.controller.controllers.CanvasController;
import com.sharedtable.controller.Command;

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
        if (isServer)
            prepareReceievingConnections(port);
    }

    //launch connection receiver thread
    public void prepareReceievingConnections(int port) {
        connectionReceiverThread = new ConnectionReceiverThread(port);
        connectionReceiverThread.start();
        openedPort = port;
        System.out.println("Prepared for receiving connections");
    }

    //make outgoing connection
    public static void connect(final String IP, int port) throws IOException {
        upperConnectedClientEntity = new ConnectedClientEntity(new Socket(IP, port), canvasController,false);
    }

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

    public static void addReceivedConnection(Socket connection) {
        try { semaphore.acquire(); } catch (Exception e) {System.out.println(e);}
        ConnectedClientEntity connectedClientEntity = new ConnectedClientEntity(connection, canvasController,true);
        connectedClientEntity.setLowerClientEntity(true);
        lowerConnectedClientEntities.add(connectedClientEntity);
        semaphore.release();
    }

    private static void reconnectToAnotherNetworkClient() {
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
    }

    public static void setUpperClientEntity(ConnectedClientEntity connectedClientEntity) {
        /*if(connectedClientEntity == null && lowerConnectedClientEntities.isEmpty() ) {
            reconnectToAnotherNetworkClient();
        }*/
        upperConnectedClientEntity = connectedClientEntity;
    }

    public static void removeClientEntity(UUID id) {
        try { semaphore.acquire(); } catch (Exception e) {System.out.println(e);}

        allNetworkClients.removeIf(act -> act.getID().equals(id));

        if(upperConnectedClientEntity != null && upperConnectedClientEntity.getUserId().equals(id)) {
            setUpperClientEntity(null);
            semaphore.release();
            return;
        }
        for(ConnectedClientEntity act : lowerConnectedClientEntities) {
            if(act.getUserId().equals(id)){
                lowerConnectedClientEntities.remove(act);
                semaphore.release();
                return;
            }
        }
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

    public static void sendMementoOpenerSignal(UUID userID,UUID mementoID,boolean isLinked) {
        sendMementoOpenerSignalDownwards(userID,mementoID,isLinked);
        sendMementoOpenerSignalUpwards(userID,mementoID,isLinked);
    }

    public static void sendMementoCloserSignal(UUID userID,UUID mementoID,boolean isLinked) {
        sendMementoCloserSignalDownwards(userID,mementoID,isLinked);
        sendMementoCloserSignalUpwards(userID,mementoID,isLinked);
    }

    public static String getMementoCloserSignal(UUID userID,UUID mementoID,boolean isLinked) {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;").append(userID.toString()).append(";CLOSE;").append(mementoID.toString()).append(";")
                .append(isLinked);
        return sb.toString();
    }

    public static String getMementoOpenerSignal(UUID userID,UUID mementoID,boolean isLinked) {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;").append(userID.toString()).append(";OPEN;").append(mementoID.toString()).append(";")
                .append(isLinked);
        return sb.toString();
    }

    public static void sendMementoOpenerSignalUpwards(UUID userID,UUID mementoID,boolean isLinked) {
        forwardMessageUpwards(getMementoOpenerSignal(userID, mementoID,isLinked));
    }

    public static void sendMementoOpenerSignalDownwards(UUID userID,UUID mementoID,boolean isLinked) {
        forwardMessageDownwards(getMementoOpenerSignal(userID, mementoID,isLinked));
    }
    public static void sendMementoCloserSignalUpwards(UUID userID,UUID mementoID,boolean isLinked) {
        forwardMessageUpwards(getMementoCloserSignal(userID,mementoID,isLinked));
    }
    public static void sendMementoCloserSignalDownwards(UUID userID,UUID mementoID,boolean isLinked) {
        forwardMessageDownwards(getMementoCloserSignal(userID,mementoID,isLinked));
    }

    public static void addNetworkClientEntity(NetworkClientEntity networkClientEntity) {
        if(!UserID.getUserID().equals(networkClientEntity.getID()) &&
                !allNetworkClients.contains(networkClientEntity))
        {
            allNetworkClients.add(networkClientEntity);
            System.out.println("NetworkClientEntity added: "+networkClientEntity.getID().toString());
        }
    }

    public static boolean isClientInNetwork(UUID clientID) {
        for(NetworkClientEntity act : allNetworkClients) {
            if(act.getID().equals(clientID)){
                return true;
            }
        }
        return false;
    }

    /*public static void sendMementoOpenerSignalToClient(UUID userID,UUID mementoID) {
        sendMessageToClient(userID,getMementoOpenerSignal(userID, mementoID));
    }

    public static void sendMementoCloserSignalToClient(UUID userID,UUID mementoID) {
        sendMessageToClient(userID,getMementoCloserSignal(userID, mementoID));
    }*/

    public static void forwardMessageUpwards(String message) {
        if(upperConnectedClientEntity != null)
            upperConnectedClientEntity.sendPlainText(message);
    }

    public static void forwardMessageDownwards(String message) {
        for(ConnectedClientEntity act : lowerConnectedClientEntities) {
            act.sendPlainText(message);
        }
    }

    public static void forwardMesageDownwardsWithException(String message, UUID except) {
        for(ConnectedClientEntity act : lowerConnectedClientEntities) {
            if(!act.getUserId().equals(except))
                act.sendPlainText(message);
        }
    }

    //!!!!!!!!!!!!!!!!!!!!!
    public static void sendMessageToClient(UUID uuid, String message) {
        ConnectedClientEntity connectedClientEntity = getClientEntityByUUID(uuid);
        connectedClientEntity.sendPlainText(message);
    }

    public static boolean isPortOpened() {return openedPort != -1;}

    public static int getOpenedPort() {
        return openedPort;
    }

    private static ConnectedClientEntity getClientEntityByUUID(UUID uuid) {
        if(upperConnectedClientEntity != null && upperConnectedClientEntity.getUserId().equals(uuid)) {
            return upperConnectedClientEntity;
        }
        for(ConnectedClientEntity act : lowerConnectedClientEntities) {
            if(act.getUserId().equals(uuid)) {
                return act;
            }
        }
        throw new RuntimeException("User nof found by ID");
    }

    public static String getPublicIP() {
        String systemipaddress = "";
        try
        {
            URL url_name = new URL("http://bot.whatismyipaddress.com");

            BufferedReader sc =
                    new BufferedReader(new InputStreamReader(url_name.openStream()));

            // reads system IPAddress
            systemipaddress = sc.readLine().trim();
        }
        catch (Exception e)
        {
            systemipaddress = "Cannot Execute Properly";
        }
        //return systemipaddress;
        return "127.0.0.1";
    }

    public static void printClientList() {
        System.out.println("---------CLIENT LIST----------");
        for(NetworkClientEntity act : allNetworkClients) {
            System.out.println(act);
        }
        System.out.println("---------CLIENT LIST END----------");
    }

    public static ArrayList<NetworkClientEntity> getAllNetworkClients() {return allNetworkClients;}

    private static ArrayList<NetworkClientEntity> allNetworkClients = new ArrayList<>();
    private static ConnectedClientEntity upperConnectedClientEntity = null;
    private static ArrayList<ConnectedClientEntity> lowerConnectedClientEntities = new ArrayList<>();
    //private static ArrayList<UUID> transitiveClientIDs = new ArrayList<>();
    private static int openedPort = -1;
    private static ConnectionReceiverThread connectionReceiverThread;
    private static CanvasController canvasController;

    private static Semaphore semaphore = new Semaphore(1);

    /*
    -ha alulról kap infót, azt felfele, és lefel is továbbküldi (értelemszerűen a forrásnak nem)
    -ha felülről jön akkor tovább küldi mindenkinek lefele
     */
}
