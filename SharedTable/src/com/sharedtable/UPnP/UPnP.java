/*
 * Copyright (C) 2015 Federico Dossena (adolfintel.com).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.sharedtable.UPnP;

/**
 * This class contains static methods that allow quick access to com.sharedtable.UPnP Port Mapping.<br>
 * Commands will be sent to the default gateway.
 *
 * @author Federico
 */
public class UPnP {

    private static Gateway defaultGW = null;
    private static int openedPort = -1;
    public static int getOpenedPort(){
        return openedPort;
    }

    private static final GatewayFinder finder = new GatewayFinder() {
        @Override
        public void gatewayFound(Gateway g) {
            synchronized (finder) {
                if (defaultGW == null) {
                    defaultGW = g;
                }
            }
        }
    };

    /**
     * Waits for com.sharedtable.UPnP to be initialized (takes ~3 seconds).<br>
     * It is not necessary to call this method manually before using com.sharedtable.UPnP functions
     */
    public static void waitInit() {
        while (finder.isSearching()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
            }
        }
    }

    /**
     * Is there an com.sharedtable.UPnP gateway?<br>
     * This method is blocking if com.sharedtable.UPnP is still initializing<br>
     * All com.sharedtable.UPnP commands will fail if com.sharedtable.UPnP is not available
     *
     * @return true if available, false if not
     */
    public static boolean isUPnPAvailable() {
        waitInit();
        return defaultGW != null;
    }

    /**
     * Opens a TCP port on the gateway
     *
     * @param port TCP port (0-65535)
     * @return true if the operation was successful, false otherwise
     */
    public static boolean openPortTCP(int port) {
        if (!isUPnPAvailable()) return false;
        if(defaultGW.openPort(port, false)){
            openedPort = port;
            return true;
        }
        return false;
    }

    /**
     * Opens a UDP port on the gateway
     *
     * @param port UDP port (0-65535)
     * @return true if the operation was successful, false otherwise
     */
    public static boolean openPortUDP(int port) {
        if (!isUPnPAvailable()) return false;
        return defaultGW.openPort(port, true);
    }

    /**
     * Closes a TCP port on the gateway<br>
     * Most gateways seem to refuse to do this
     *
     * @param port TCP port (0-65535)
     * @return true if the operation was successful, false otherwise
     */
    public static boolean closePortTCP(int port) {
        if (!isUPnPAvailable()) return false;
        if( defaultGW.closePort(port, false)){
            if(openedPort == port)
                openedPort =-1;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Closes a UDP port on the gateway<br>
     * Most gateways seem to refuse to do this
     *
     * @param port UDP port (0-65535)
     * @return true if the operation was successful, false otherwise
     */
    public static boolean closePortUDP(int port) {
        if (!isUPnPAvailable()) return false;
        return defaultGW.closePort(port, true);
    }

    /**
     * Checks if a TCP port is mapped<br>
     *
     * @param port TCP port (0-65535)
     * @return true if the port is mapped, false otherwise
     */
    public static boolean isMappedTCP(int port) {
        if (!isUPnPAvailable()) return false;
        return defaultGW.isMapped(port, false);
    }

    /**
     * Checks if a UDP port is mapped<br>
     *
     * @param port UDP port (0-65535)
     * @return true if the port is mapped, false otherwise
     */
    public static boolean isMappedUDP(int port) {
        if (!isUPnPAvailable()) return false;
        return defaultGW.isMapped(port, false);
    }

    /**
     * Gets the external IP address of the default gateway
     *
     * @return external IP address as string, or null if not available
     */
    public static String getExternalIP() {
        if (!isUPnPAvailable()) return null;
        return defaultGW.getExternalIP();
    }

    /**
     * Gets the internal IP address of this machine
     *
     * @return internal IP address as string, or null if not available
     */
    public static String getLocalIP() {
        if (!isUPnPAvailable()) return null;
        return defaultGW.getLocalIP();
    }

}
