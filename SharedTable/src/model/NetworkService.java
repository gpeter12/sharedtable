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
        throw new UnsupportedOperationException();
    }

    //connection receiver thread
    public void receivingConnections() {
        throw new UnsupportedOperationException();
    }

    //make outgoing connection
    public void connect(String IP,int port) {
        throw new UnsupportedOperationException();
    }

    //disconnect all incoming connections and the outgoing
    public void disconnect() {
        throw new UnsupportedOperationException();
    }

    //open an available local port for receiving remote incoming connections
    private void openLocalPort(boolean withUPnP) {
        throw new UnsupportedOperationException();
    }

    //tries to find a random availeble port for incoming connections from 50000 to 55000
    private int findOpenableLocalPort() {
        throw new UnsupportedOperationException();
    }

    //opens a ServerSocket
    public Socket getLocalPort(int port) {
        throw new UnsupportedOperationException();
    }

    //sends all data to clients that connected to this client
    public void propagateDataDownwards(ArrayList<StateMemento> data) {
        throw new UnsupportedOperationException();
    }

    //sends data to the client that this client connected
    public void propagateDataUpwards(ArrayList<StateMemento> data) {
        throw new UnsupportedOperationException();
    }

    private ArrayList<Socket> receivedConnections = new ArrayList<>();
    private Socket outgoingConnection;

    //connectedTOMe socket list
    //Im connected to sb socket

    /*
    -ha alulról kap infót, azt felfele, és lefel is továbbküldi (értelemszerűen a forrásnak nem)
    -ha felülről jön akkor tovább küldi mindenkinek lefele
     */
}
