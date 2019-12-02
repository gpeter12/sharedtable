package model;

import java.io.IOException;
import java.net.ServerSocket;

public class ConnectionReceiverThread extends Thread {

    public ConnectionReceiverThread(int port) {
        try {
            serverSocket = new ServerSocket(port);
            /*if(!UPnP.isUPnPAvailable()) {
                //TODO rendesen lekezelni, ablakos hibaüzenettel és javalattal
                throw new RuntimeException("UPnP is not available");
            } else {
                //TODO rendesen portot választani
                //UPnP.openPortTCP(2222);
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
        } catch (Exception e) {
            throw new RuntimeException("Error during accepting socket\n" + e);
        }
    }

    public void timeToStop() {
        timeToStop = true;
        //UPnP.closePortTCP(2222);
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
