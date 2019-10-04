package model;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class LowerClientEntity extends ClientEntity{

    public LowerClientEntity(Socket socket) throws IOException {
        super(socket);
    }



}
