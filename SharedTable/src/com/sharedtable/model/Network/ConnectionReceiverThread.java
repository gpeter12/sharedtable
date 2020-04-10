package com.sharedtable.model.Network;

import com.sharedtable.UPnP.UPnP;
import com.sharedtable.UPnP.UPnPConfigException;

import java.io.IOException;
import java.net.ServerSocket;

public class ConnectionReceiverThread extends Thread {

    public ConnectionReceiverThread(int port) throws IOException, UPnPConfigException {
        ServerSocket ss = new ServerSocket(port);

        if(!UPnP.openPortTCP(port)) {
            throw new UPnPConfigException();
        }

        serverSocket = ss;
        openedPort = serverSocket.getLocalPort();

    }

    @Override
    public void run() {
        try {
            while (!timeToStop) {
                NetworkService.addReceivedConnection(serverSocket.accept());
                System.out.println("Connection received!");
            }
        } catch (IOException e) {
            if(timeToStop){
                System.out.println("connection receiver thread shutting down");
            }

        }
    }

    public int getOpenedPort() {
        return openedPort;
    }

    public void timeToStop() {
        timeToStop = true;
        //com.sharedtable.UPnP.closePortTCP(2222);
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error during closing server socket\n" + e);
        }
        interrupt();
    }

    private boolean timeToStop = false;
    private ServerSocket serverSocket;
    private int openedPort;
}
