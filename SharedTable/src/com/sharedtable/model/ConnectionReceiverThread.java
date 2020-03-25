package com.sharedtable.model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

public class ConnectionReceiverThread extends Thread {

    public ConnectionReceiverThread(int port) {
        try {
            serverSocket = new ServerSocket(port);
            /*if(!com.sharedtable.UPnP.isUPnPAvailable()) {
                //TODO rendesen lekezelni, ablakos hibaüzenettel és javalattal
                throw new RuntimeException("com.sharedtable.UPnP is not available");
            } else {
                //TODO rendesen portot választani
                //com.sharedtable.UPnP.openPortTCP(2222);
            }*/

        } catch (Exception e) {
            throw new RuntimeException("Error during server socket initialization\n" + e);
        }
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
}
