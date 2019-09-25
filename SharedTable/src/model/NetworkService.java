package model;

import controller.StateMemento;

import java.net.Socket;
import java.util.ArrayList;

public class NetworkService {

    public NetworkService() {
        prepareReceievingConnections();
    }

    //launch connection receiver thread
    public void prepareReceievingConnections() {

    }

    //connection receiver thread
    public void receivingConnections() {

    }

    //make outgoing connection
    public void connect(String IP,int port) {

    }

    //disconnect all incoming connections and the outgoing
    public void disconnect() {

    }

    //open an available local port for receiving remote incoming connections
    private void openLocalPort(boolean withUPnP) {

    }

    //tries to find a random availeble port for incoming connections from 50000 to 55000
    private int findOpenableLocalPort() {
        return 0;
    }

    //opens a ServerSocket
    public Socket getLocalPort(int port) {
        return null;
    }

    public void propagateDataDownwards(ArrayList<StateMemento> data) {

    }

    public void propagateDataUpwards(ArrayList<StateMemento> data) {

    }

    private ArrayList<Socket> receivedConnections = new ArrayList<>();
    private Socket outgoingConnection;

    //connectedTOMe socket list
    //I connected to sb socket

    /*
    -ha alulról kap infót, azt felfele, és lefel is továbbküldi (értelemszerűen a forrásnak nem
    -ha felülről jön akkor tovább küldi mindenkinek lefele
     */
}
