package model;

import controller.CanvasController;
import controller.Command;
import controller.CommandFactory;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientEntity extends Thread {

    public ClientEntity(Socket socket, CanvasController canvasController) {
        this.socket = socket;
        this.canvasController = canvasController;
        try {
            initializeStreams(socket);
        } catch (Exception e) {
            throw new RuntimeException("Inititalization of input/output network streams failed.");
        }
        start();
    }

    @Override
    public void run() {
        System.out.println("run()");
        while (scanner.hasNext()) {
            canvasController.processRemoteCommand(receiveCommand());
        }
        scanner.close();
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println("end run()");
    }

    private void initializeStreams(Socket socket) throws IOException {
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        scanner = new Scanner(new InputStreamReader(inputStream));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        System.out.println("outgoing connections's I/O streams are initialized!");
    }

    public void sendCommand(Command command) {
        System.out.println("command sending: "+command.toString());
        try{
            bufferedWriter.write(command.toString());
            bufferedWriter.flush();
        } catch (Exception e) {
            System.out.println("TheException"+e);
        }
        System.out.println("command sent: "+command.toString());
    }

    public Command receiveCommand() {
        String receivedCommand = scanner.nextLine();
        System.out.println("received command: "+receivedCommand);
        if (!receivedCommand.isEmpty())
            return CommandFactory.getCommand(receivedCommand,canvasController.getMainCanvas());
        else
            throw new RuntimeException("input command data is empty!");
    }

    Socket socket;
    OutputStream outputStream;
    InputStream inputStream;
    BufferedWriter bufferedWriter;
    Scanner scanner;
    CanvasController canvasController;


    public void timeToStop() {
        interrupt();
    }
}
