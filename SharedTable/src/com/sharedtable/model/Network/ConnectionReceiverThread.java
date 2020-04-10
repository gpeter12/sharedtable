package com.sharedtable.model.Network;

import com.sharedtable.LoggerConfig;
import com.sharedtable.model.Network.UPnP.UPnPConfigException;
import com.sharedtable.model.Network.UPnP.UPnPHandler;
import com.sharedtable.view.MainView;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

public class ConnectionReceiverThread extends Thread {

    public ConnectionReceiverThread(int port) throws IOException, UPnPConfigException {
        logger = LoggerConfig.setLogger(Logger.getLogger(MainView.class.getName()));

        ServerSocket ss = new ServerSocket(port);

        UPnPHandler.openPort(port);

        serverSocket = ss;
        openedPort = serverSocket.getLocalPort();

    }

    @Override
    public void run() {
        try {
            while (!timeToStop) {
                NetworkService.addReceivedConnection(serverSocket.accept());
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
        //com.sharedtable.UPnP.closePortTCP(2222);
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
    private int openedPort;
    private static Logger logger = null;
}
