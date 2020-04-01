package com.sharedtable.controller;

import java.util.UUID;

public class UserID {

    private UserID() {

    }

    public static UUID getUserID() {
        return userID;
    }

    public static String getNickname() {
        return nickname;
    }

    public static String getPublicIP() {
        return IP;
    }

    private static UUID generateUserID() {
        UUID uuid = UUID.randomUUID();
        System.out.println("MY USR ID: "+uuid.toString());
        System.out.println("-----------------");
        return uuid;
    }

    private static String getPublicIPFromWeb() {
        /*String systemipaddress = "";
        try {
            URL url_name = new URL("http://bot.whatismyipaddress.com");

            BufferedReader sc =
                    new BufferedReader(new InputStreamReader(url_name.openStream()));

            // reads system IPAddress
            systemipaddress = sc.readLine().trim();
        } catch (Exception e) {
            systemipaddress = "Cannot Execute Properly";
        }
        //return systemipaddress;*/
        return "127.0.0.1";
    }

    private static final UUID userID = generateUserID();
    private static final String nickname = "nickname";
    private static final String IP = getPublicIPFromWeb();
}
