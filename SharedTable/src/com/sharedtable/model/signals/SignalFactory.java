package com.sharedtable.model.signals;

import com.sharedtable.model.ArrayPrinter;

public class SignalFactory {

    public static boolean isSignal(String[] input) {
        if(input[0].equals("SIG"))
            return true;
        return false;
    }

    public static Signal getSignal(String[] input) {
        if(!isSignal(input))
            throw new RuntimeException("input is not a signal! "+ ArrayPrinter.printStringArray(input));
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
        else if(isSyncSignal(input))
            return new SyncedSignal(input);
        else if(isChatMessageSignal(input))
            return new ChatMessageSignal(input);
        else if(isByteReceiveReadySignal(input))
            return new ByteReceiveReadySignal(input);
        else if(isChangePasswordSignal(input))
            return new NetworkPasswordChangeSignal(input);
        else if(isRenameTabSignal(input))
            return new RenameTabSignal(input);
        else

            throw new RuntimeException("Unrecognised signal! "+ArrayPrinter.printStringArray(input));
    }

    private static boolean isRenameTabSignal(String[] input) {
        if(input[1].equals("TABRENAME"))
            return true;
        return false;
    }

    private static boolean isChangePasswordSignal(String[] input) {
        if(input[1].equals("PASSWD"))
            return true;
        return false;
    }

    private static boolean isByteReceiveReadySignal(String[] input) {
        if(input[1].equals("BRECREADY"))
            return true;
        return false;
    }

    private static boolean isChatMessageSignal(String[] input) {
        if(input[1].equals("CHAT"))
            return true;
        return false;
    }

    private static boolean isSyncSignal(String[] input) {
        if(input[1].equals("SYNCED"))
            return true;
        return false;
    }

    private static boolean isCloseTabSignal(String[] input) {
        if(input[1].equals("CLOSETAB"))
            return true;
        return false;
    }

    private static boolean isNewTabSignal(String[] input) {
        if(input[1].equals("NEWTAB"))
            return true;
        return false;
    }

    private static boolean isDiscoverySignal(String[] input) {
        if(input[1].equals("DISCOV"))
            return true;
        return false;
    }

    private static boolean isPingSignal(String[] input) {
        if(input[1].equals("PING"))
            return true;
        return false;
    }


    private static boolean isEntityTreeSignal(String[] input) {
        if(input[1].equals("TREE"))
            return true;
        return false;
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
