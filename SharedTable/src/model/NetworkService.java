package model;

import controller.CanvasController;
import controller.Command;

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
            upperClientEntity = new ClientEntity(new Socket(IP, port), canvasController);

        } catch (Exception e) {
            throw new RuntimeException("Error during connect to another client" + "\nEXCEPTION: " + e);
        }
        //TODO: Synchronize
    }

    //disconnect all incoming connections and the outgoing
    public static void disconnect() {
        throw new UnsupportedOperationException();
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
        lowerClientEntities.add(new ClientEntity(connection, canvasController));
    }

    public static void timeToStop() {
        connectionReceiverThread.timeToStop();
        upperClientEntity.timeToStop();
        for (ClientEntity act : lowerClientEntities) {
            act.timeToStop();
        }
    }

    public static void sendMementoOpenerSignal(UUID userID,UUID mementoID) {
        StringBuilder sb = new StringBuilder();
        sb.append(userID.toString()).append(";OPEN;").append(mementoID.toString());
        forwardMessageDownwards(sb.toString());
        forwardMessageUpwards(sb.toString());
    }

    public static void sendMementoCloserSignal(UUID userID,UUID mementoID) {
        StringBuilder sb = new StringBuilder();
        sb.append(userID.toString()).append(";CLOSE;").append(mementoID.toString());
        forwardMessageDownwards(sb.toString());
        forwardMessageUpwards(sb.toString());
    }

    public static void forwardMessageUpwards(String message) {
        if(upperClientEntity != null)
            upperClientEntity.sendPlainText(message);
    }

    public static void forwardMessageDownwards(String message) {
        for(ClientEntity act : lowerClientEntities) {
            act.sendPlainText(message);
        }
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
