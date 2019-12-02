/*package model;

import controller.CanvasController;
import controller.StateMemento;
import view.MainCanvas;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class UpperClientEntity extends ClientEntity {

    public UpperClientEntity(String ip, int port, CanvasController canvasController) throws IOException {
        super(new Socket(ip, port), canvasController);
    }

    public ArrayList<StateMemento> downloadAllCommands() throws IOException {
        throw new UnsupportedOperationException();
        /*ArrayList<StateMemento> ret = new ArrayList<>();
        while(bufferedReader.ready()) {
            ret.add(receiveState());
        }
        return ret;
    }


}
            */