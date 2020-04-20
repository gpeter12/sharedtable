package com.sharedtable.model.network;

import com.sharedtable.view.MainView;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

public class ConnectionReceiverThread extends Thread {

    public ConnectionReceiverThread(int port) throws IOException {
        logger = Logger.getLogger(MainView.class.getName());
        serverSocket = new ServerSocket(port);
        serverSocket2 = new ServerSocket(port+1);
        openedPort = serverSocket.getLocalPort();
    }

    @Override
    public void run() {
        setName("ConnectionReceiverThread");
        try {
            while (!timeToStop) {
                NetworkService.addReceivedConnection(serverSocket.accept(),serverSocket2.accept());
                logger.info("Connection received!");
            }
        } catch (IOException e) {
            if(timeToStop){
                logger.info("connection receiver thread shutting down");
            }

        }
    }

    public int getOpenedPort() {
        return openedPort;
    }

    public void timeToStop() {
        logger.info("closing server socket...");
        timeToStop = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.info("Error during closing server socket\n" + e);
            throw new RuntimeException("Error during closing server socket\n" + e);
        }
        interrupt();
    }

    private boolean timeToStop = false;
    private ServerSocket serverSocket;
    private ServerSocket serverSocket2;
    private int openedPort;
    private static Logger logger = Logger.getLogger(MainView.class.getName());
}
