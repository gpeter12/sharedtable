package model;

import controller.StateMemento;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class UpperClientEntity {

    public UpperClientEntity(String ip, int port) throws IOException {
        outgoingConnection = new Socket(ip, port);
        outputStream = outgoingConnection.getOutputStream();
    }

    public ArrayList<StateMemento> downloadAllMementos() throws IOException {
        ArrayList<StateMemento> ret = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        /*while(reader.ready())
        String mementoData = reader.readLine();*/

    }

    public void sendState(StateMemento memento) {
        throw new UnsupportedOperationException();
    }

    public StateMemento receiveState() {
        throw new UnsupportedOperationException();
    }

    Socket outgoingConnection;
    OutputStream outputStream;
    InputStream inputStream;
}
