package model;

import controller.CanvasController;
import view.MainCanvas;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class LowerClientEntity extends ClientEntity{

    public LowerClientEntity(Socket socket, CanvasController canvasController) {
        super(socket,canvasController);
    }



}
