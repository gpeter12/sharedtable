package com.sharedtable.model.network.signals;

import com.sharedtable.Utils;

public class SignalFactory {

    public static boolean isSignal(String[] input) {
        if(input[0].equals("SIG"))
            return true;
        return false;
    }

    public static Signal getSignal(String[] input) {
        if(!isSignal(input))
            throw new RuntimeException("input is not a signal! "+ Utils.recombineStringArray(input));
        if(isMementoOpenerSignal(input))
            return new MementoOpenerSignal(input);
        else if(isMementoCloserSignal(input))
            return new MementoCloserSignal(input);
        else if(isNewClientSignal(input))
            return new NewClientSignal(input);
        else if(isDisconnectionSignal(input))
            return new DisconnectSignal(input);
        else if(isEntityTreeSignal(input))
            return new EntityTreeSignal(input);
        else if(isPingSignal(input))
            return new PingSignal(input);
        else if(isDiscoverySignal(input))
            return new DiscoverySignal(input);
        else if(isCloseTabSignal(input))
            return new CloseTabSignal(input);
        else if(isNewTabSignal(input))
            return new NewTabSignal(input);
        else if(isChatMessageSignal(input))
            return new ChatMessageSignal(input);
        else if(isChangePasswordSignal(input))
            return new NetworkPasswordChangeSignal(input);
        else if(isRenameTabSignal(input))
            return new RenameTabSignal(input);
        else if(isDeleteAfterSignal(input))
            return new DeleteAfterSignal(input);
        else
            throw new RuntimeException("Unrecognised signal! "+ Utils.recombineStringArray(input));
    }

    private static boolean isDeleteAfterSignal(String[] input) { return input[1].equals("DELAFTER"); }

    private static boolean isRenameTabSignal(String[] input) { return input[1].equals("TABRENAME"); }

    private static boolean isChangePasswordSignal(String[] input) { return input[1].equals("PASSWD"); }

    private static boolean isChatMessageSignal(String[] input) { return input[1].equals("CHAT"); }

    private static boolean isCloseTabSignal(String[] input) { return input[1].equals("CLOSETAB"); }

    private static boolean isNewTabSignal(String[] input) { return input[1].equals("NEWTAB"); }

    private static boolean isDiscoverySignal(String[] input) { return input[1].equals("DISCOV"); }

    private static boolean isPingSignal(String[] input) { return input[1].equals("PING"); }

    private static boolean isEntityTreeSignal(String[] input) { return input[1].equals("TREE"); }

    private static boolean isNewClientSignal(String[] input) { return input[1].equals("CONN"); }

    private static boolean isDisconnectionSignal(String[] input) { return input[1].equals("DISCONN"); }

    private static boolean isMementoOpenerSignal(String[] input) { return input[2].equals("OPEN"); }

    private static boolean isMementoCloserSignal(String[] input) { return input[2].equals("CLOSE"); }
}
