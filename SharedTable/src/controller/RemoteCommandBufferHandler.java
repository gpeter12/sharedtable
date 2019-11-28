package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class RemoteCommandBufferHandler {

    public RemoteCommandBufferHandler() {

    }

    public static void addCommand(Command command) {

    }

    private static boolean isMementoOpener(String[] input) {
        if(input[1].equals("OPEN")) {
            return true;
        }
        return false;
    }

    private static boolean isMementoCloser(String[] input) {
        if(input[1].equals("CLOSE")) {
            return true;
        }
        return false;
    }

    private static ArrayList<Command> getBufferByID(String[] input) {
        UUID userID = UUID.fromString(input[0]);
        if(commandBuffers.containsKey(userID))
            return commandBuffers.get(userID);
        else {
            commandBuffers.put(userID,new ArrayList<Command>());
            return commandBuffers.get(userID);
        }
    }

    private static HashMap<UUID, ArrayList<Command>> commandBuffers = new HashMap<>();

}
