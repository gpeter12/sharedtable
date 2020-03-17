package com.sharedtable.model;

import com.sharedtable.controller.controllers.CanvasController;
import com.sharedtable.controller.Command;

import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

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
        System.out.println("Prepared for receiving connections");
    }

    //make outgoing connection
    public static void connect(final String IP, int port) {
        try {
            upperClientEntity = new ClientEntity(new Socket(IP, port), canvasController,false);

        } catch (Exception e) {
            throw new RuntimeException("Error during connect to another client" + "\nEXCEPTION: " + e);
        }
        //TODO: Synchronize
    }

    //sends all data to clients that connected to this client
    public static void propagateCommandDownwards(Command command) {
        for (ClientEntity act : lowerClientEntities) {
            try {
                act.sendCommand(command);
            } catch (Exception e) {
                throw new RuntimeException("An error occured during sending commands downwards");
            }
        }
    }

    //sends data to the client that this client connected
    public static void propagateCommandUpwards(Command command) {
        if (upperClientEntity != null) {
            upperClientEntity.sendCommand(command);
        }
    }

    public static void addReceivedConnection(Socket connection) {
        ClientEntity clientEntity = new ClientEntity(connection, canvasController,true);
        clientEntity.setLowerClientEntity(true);
        lowerClientEntities.add(clientEntity);
    }

    public static void removeClientEntity(UUID id) {
        if(upperClientEntity != null && upperClientEntity.getUserId().equals(id)) {
            upperClientEntity = null;
            return;
        }
        for(ClientEntity act : lowerClientEntities) {
            if(act.getUserId().equals(id)){
                lowerClientEntities.remove(act);
                return;
            }
        }


    }

    public static void timeToStop() {
        connectionReceiverThread.timeToStop();
        if(upperClientEntity != null)
            upperClientEntity.timeToStop();
        for (ClientEntity act : lowerClientEntities) {
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
        sb.append(userID.toString()).append(";CLOSE;").append(mementoID.toString()).append(";")
                .append(isLinked);
        return sb.toString();
    }

    public static String getMementoOpenerSignal(UUID userID,UUID mementoID,boolean isLinked) {
        StringBuilder sb = new StringBuilder();
        sb.append(userID.toString()).append(";OPEN;").append(mementoID.toString()).append(";")
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

    /*public static void sendMementoOpenerSignalToClient(UUID userID,UUID mementoID) {
        sendMessageToClient(userID,getMementoOpenerSignal(userID, mementoID));
    }

    public static void sendMementoCloserSignalToClient(UUID userID,UUID mementoID) {
        sendMessageToClient(userID,getMementoCloserSignal(userID, mementoID));
    }*/

    public static void forwardMessageUpwards(String message) {
        if(upperClientEntity != null)
            upperClientEntity.sendPlainText(message);
    }

    public static void forwardMessageDownwards(String message) {
        for(ClientEntity act : lowerClientEntities) {
            act.sendPlainText(message);
        }
    }

    public static void forwardMesageDownwardsWithException(String message, UUID except) {
        for(ClientEntity act : lowerClientEntities) {
            if(!act.getUserId().equals(except))
                act.sendPlainText(message);
        }
    }

    //!!!!!!!!!!!!!!!!!!!!!
    public static void sendMessageToClient(UUID uuid, String message) {
        ClientEntity clientEntity = getClientEntityByUUID(uuid);
        clientEntity.sendPlainText(message);
    }

    private static ClientEntity getClientEntityByUUID(UUID uuid) {
        if(upperClientEntity != null && upperClientEntity.getUserId().equals(uuid)) {
            return upperClientEntity;
        }
        for(ClientEntity act : lowerClientEntities) {
            if(act.getUserId().equals(uuid)) {
                return act;
            }
        }
        throw new RuntimeException("User nof found by ID");
    }

    private static ClientEntity upperClientEntity = null;
    private static ArrayList<ClientEntity> lowerClientEntities = new ArrayList<>();

    private static ConnectionReceiverThread connectionReceiverThread;
    private static CanvasController canvasController;

    /*
    -ha alulról kap infót, azt felfele, és lefel is továbbküldi (értelemszerűen a forrásnak nem)
    -ha felülről jön akkor tovább küldi mindenkinek lefele
     */
}
