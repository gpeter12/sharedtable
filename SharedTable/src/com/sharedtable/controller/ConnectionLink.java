package com.sharedtable.controller;

public class ConnectionLink {

    public ConnectionLink(String link) {
        processLink(link);
    }

    private void processLink(String input) {
        String[] parts = input.split("stconnect://");
        if(!input.startsWith("stconnect://")){
            throw new IllegalArgumentException("Invalid prefix");
        }
        String[] ipAndPort = parts[1].split(":");
        if(ipAndPort.length != 2){
            throw new IllegalArgumentException("invalid link component number");
        }

        String IP = ipAndPort[0];
        int port = Integer.parseInt(ipAndPort[1]);

        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Invalid port number");
        }
        if (!validateIP(IP)) {
            throw new IllegalArgumentException("Invalid IP address");
        }

        this.port = port;
        this.IP = IP;
    }

    private boolean validateIP(String ip) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(PATTERN);
    }

    public int getPort() {
        return port;
    }

    public String getIP() {
        return IP;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("stconnect://").append(IP).append(":").append(port);
        return sb.toString();
    }

    private int port;
    private String IP;
}
