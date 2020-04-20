package com.sharedtable.model.network.UPnP;
import com.dosse.upnp.UPnP;

public class UPnPHandler {
    public static void openPort(int port) throws UPnPConfigException {
        if(!UPnP.openPortTCP(port)){
            throw new UPnPConfigException();
        } else {
            openedPort = port;
        }
    }

    public static void closePort(int port) throws UPnPConfigException {
        if(!UPnP.closePortTCP(port)){
            throw new UPnPConfigException();
        } else {
            openedPort = -1;
        }
    }

    public static String getExternalIP() throws UPnPConfigException {
        if(!UPnP.isUPnPAvailable())
            throw new UPnPConfigException();
        return UPnP.getExternalIP();
    }

    public static int getOpenedPort(){
        return openedPort;
    }

    private static int openedPort = -1;
}
