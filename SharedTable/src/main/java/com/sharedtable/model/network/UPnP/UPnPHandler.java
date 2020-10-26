package com.sharedtable.model.network.UPnP;

import com.dosse.upnp.UPnP;

public class UPnPHandler {

    private static UPnPHandler instance = new UPnPHandler();
    private int openedPort = -1;

    private UPnPHandler() {}

    public static UPnPHandler getInstance() {
        if(instance == null) {
            instance = new UPnPHandler();
        }
        return instance;
    }

    public void openPort(int port) throws UPnPConfigException {
        if(!UPnP.openPortTCP(port)){
            throw new UPnPConfigException();
        } else {
            openedPort = port;
        }
    }

    public void closePort(int port) throws UPnPConfigException {
        if(!UPnP.closePortTCP(port)){
            throw new UPnPConfigException();
        } else {
            openedPort = -1;
        }
    }

    public String getExternalIP() throws UPnPConfigException {
        if(!UPnP.isUPnPAvailable())
            throw new UPnPConfigException();
        if(UPnP.getExternalIP().equals("0.0.0.0")) {
            return UPnP.getLocalIP();
        } else {
            return UPnP.getExternalIP();
        }
    }

    public int getOpenedPort(){
        return openedPort;
    }


}
