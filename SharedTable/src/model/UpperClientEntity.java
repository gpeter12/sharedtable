package model;

import controller.StateMemento;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class UpperClientEntity extends ClientEntity {

    public UpperClientEntity(String ip, int port) throws IOException {
        super(new Socket(ip, port));
    }

    public ArrayList<StateMemento> downloadAllMementos() throws IOException {
        ArrayList<StateMemento> ret = new ArrayList<>();
        while(bufferedReader.ready()) {
            ret.add(receiveState());
        }
        return ret;
    }






}
