package model;

import controller.StateMemento;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientEntity {

    public ClientEntity(Socket socket) throws IOException {
        this.socket = socket;
        initializeStreams(socket);
    }

    /*public ClientEntity(Socket socket) throws IOException {
        this.socket = socket;
        initializeStreams();
    }*/

    private void initializeStreams(Socket socket) throws IOException {
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    public void sendState(StateMemento memento) throws IOException {
        bufferedWriter.write(memento.toString());
    }

    public StateMemento receiveState() throws IOException {
        String mementoData = bufferedReader.readLine();
        if (!mementoData.isEmpty())
            return new StateMemento(mementoData);
        else
            throw new RuntimeException("input memento data is empty!");
    }

    Socket socket;
    OutputStream outputStream;
    InputStream inputStream;
    BufferedWriter bufferedWriter;
    BufferedReader bufferedReader;


}
