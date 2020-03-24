package com.sharedtable.model.signals;

import java.util.UUID;

public class SignalFactory {

    public static boolean isSignal(String[] input) {
        if(input[0].equals("SIG"))
            return true;
        return false;
    }

    public static Signal getSignal(String[] input) {
        if(!isSignal(input))
            throw new RuntimeException("input is not a signal! "+input.toString());
        if(isMementoOpenerSignal(input))
            return new MementoOpenerSignal(input);
        else if(isMementoCloserSignal(input))
            return new MementoCloserSignal(input);
        else if(isNewRootSignal(input))
            return new NewRootSignal(input);
        else if(isNewClientSignal(input))
            return new NewClientSignal(input);
        else if(isDisconnectionSignal(input))
            return new DisconnectSignal(input);
        else
            throw new RuntimeException("Unrecognised signal! "+input.toString());
    }

    private static boolean isNewRootSignal(String[] input) {
        if(input[1].equals("NEWROOT"))
            return true;
        return false;
    }

    private static boolean isNewClientSignal(String[] input) {
        if(input[1].equals("CONN"))
            return true;
        return false;
    }

    private static boolean isDisconnectionSignal(String[] input) {
        if(input[1].equals("DISCONN"))
            return true;
        return false;
    }

    private static boolean isMementoOpenerSignal(String[] input) {
        if (input[2].equals("OPEN")) {
            return true;
        }
        return false;
    }

    private static boolean isMementoCloserSignal(String[] input) {
        if (input[2].equals("CLOSE")) {
            return true;
        }
        return false;
    }
}
