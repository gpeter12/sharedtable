package model;

import controller.CanvasController;
import controller.Command;
import controller.StateMemento;
import view.MainCanvas;

import java.lang.reflect.Array;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

public class NetworkService {

    public NetworkService(boolean isServer, CanvasController canvasController) {
        NetworkService.canvasController = canvasController;
        if(isServer)
            prepareReceievingConnections();
    }

    //launch connection receiver thread
    public void prepareReceievingConnections() {
        connectionReceiverThread = new ConnectionReceiverThread();
        connectionReceiverThread.start();
        System.out.println("Prepared for receiving connections");
    }

    //make outgoing connection
    public static void connect(final String IP,int port) {
        try{
            upperClientEntity = new UpperClientEntity(IP,port,canvasController);

        } catch (Exception e) {
            throw new RuntimeException("Error during connect to another client"+"\nEXCEPTION: "+e);
        }
        //TODO: Synchronize
    }

    //disconnect all incoming connections and the outgoing
    public static void disconnect() {
        throw new UnsupportedOperationException();
    }

    //opens a ServerSocket
    /*private Socket openLocalPort(boolean withUPnP) {


    }*/

    //sends all data to clients that connected to this client
    public static void propagateDataDownwards(Command command) {
        for (LowerClientEntity act: lowerClientEntities) {
            try {
                act.sendCommand(command);
            } catch (Exception e) {
                throw new RuntimeException("An error occured during sending commands downwards");
            }
        }
    }

    //sends data to the client that this client connected
    public static void propagateDataUpwards(Command command) {
        //try {
        if(upperClientEntity != null) {
            upperClientEntity.sendCommand(command);
        }
        /*} catch (Exception e) {
            System.out.println(e.toString());
            throw new RuntimeException("An error occured during sending commands upwards");

        }*/
    }

    public static void addReceivedConnection(Socket connection) {
        lowerClientEntities.add(new LowerClientEntity(connection,canvasController));
    }

    public static void timeToStop() {
        connectionReceiverThread.timeToStop();
        upperClientEntity.timeToStop();
        for (LowerClientEntity act:lowerClientEntities) {
            act.timeToStop();
        }
        //throw new UnsupportedOperationException();
    }

    private static UpperClientEntity upperClientEntity = null;
    private static ArrayList<LowerClientEntity> lowerClientEntities = new ArrayList<>();

    /*private ArrayList<Socket> incomingConnections = new ArrayList<>();
    private Socket outgoingConnection;*/
    private static ConnectionReceiverThread connectionReceiverThread;
    private static CanvasController canvasController;


    //connectedTOMe socket list
    //Im connected to sb socket

    /*
    -ha alulról kap infót, azt felfele, és lefel is továbbküldi (értelemszerűen a forrásnak nem)
    -ha felülről jön akkor tovább küldi mindenkinek lefele
     */
}
