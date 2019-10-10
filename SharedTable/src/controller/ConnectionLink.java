package controller;

public class ConnectionLink {

    public ConnectionLink(String link) {
        processLink(link);
    }



    private void processLink(String input) {
        String[] parts = input.split("stconnect://");
        String[] portAndIP = parts[1].split("@");
        int port = Integer.parseInt(portAndIP[0]);
        String IP = portAndIP[1];

        if(port<1024 || port>65535) {
            throw new RuntimeException("Invalid port number");
        }
        if(!validateIP(IP)) {
            throw new RuntimeException("Invalid IP address");
        }

        this.port = port;
        this.IP = IP;
    }

    private static boolean validateIP(String ip) {
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
        return "ConnectionLink{" +
                "port=" + port +
                ", IP='" + IP + '\'' +
                '}';
    }

    private int port;
    private String IP;

}