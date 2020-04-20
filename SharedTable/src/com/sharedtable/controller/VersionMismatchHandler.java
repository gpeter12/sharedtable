package com.sharedtable.controller;

import com.sharedtable.view.MessageBox;

public class VersionMismatchHandler {

    enum MismatchType {
        ThisClientNewer,
        OtherClientNewer,
        Equals
    }

    private static MismatchType getMismatchType(int input) {
        switch (input){
            case -1:
                return MismatchType.ThisClientNewer;
            case 0:
                return MismatchType.Equals;
            case 1:
                return MismatchType.OtherClientNewer;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static void HandleVersionMismatch(int input) {
        switch (getMismatchType(input)) {
            case Equals:
                break;
            case ThisClientNewer:
                break;
            case OtherClientNewer:
                MessageBox.showWarning("Frissítés szükséges!","A kliens amivel most épült ki a kapcsolat\n frissebb mint a te kliensed!\n" +
                        "Inkompatibilitásból fakadó hibák következhetnek!");
                //new UpdateNotificationView();
        }
    }
}
