package model;

import controller.CanvasController;
import controller.Command;
import controller.CommandFactory;
import controller.RemoteCommandBufferHandler;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;

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
        while (scanner.hasNext()) {
            Command recvdCmd = receiveCommand();
            if(recvdCmd!=null)
                canvasController.processRemoteCommand(recvdCmd);
        }
        scanner.close();
        try {
            socket.close();
            interrupt();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void initializeStreams(Socket socket) throws IOException {
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        scanner = new Scanner(new InputStreamReader(inputStream));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        System.out.println("outgoing connections's I/O streams are initialized!");
    }

    public void sendCommand(Command command) {
        //System.out.println("command sending: " + command.toString());
        sendPlainText(command.toString());
        //System.out.println("command sent: " + command.toString());
    }

    public void sendPlainText(String input) {
        try {
            bufferedWriter.write(input+"\n");
            bufferedWriter.flush();
        } catch (Exception e) {
            System.out.println("Exception happened during sending plain text" + e);
        }
    }

    public Command receiveCommand() {
        String receivedCommand = scanner.nextLine();
        //System.out.println("received command: " + receivedCommand);
        String[] splittedCommand = receivedCommand.split(";");
        if (!receivedCommand.isEmpty()) {
            if(handleMementoBarrierSignal(splittedCommand))
                return null;
            else {
                Command rcvdcmd = CommandFactory.getCommand(splittedCommand, canvasController);
                RemoteCommandBufferHandler.addCommand(rcvdcmd);
                return rcvdcmd;
            }

        }
        else {
            System.out.println("input command data is empty!");
            return null;
        }
    }

    private boolean handleMementoBarrierSignal(String[] input) {
        if(isMementoOpener(input)){
            RemoteCommandBufferHandler.openNewMemento(UUID.fromString(input[0]),UUID.fromString(input[2]));
            return true;
        }
        if(isMementoCloser(input)){
            RemoteCommandBufferHandler.closeMemento(UUID.fromString(input[0]),UUID.fromString(input[2]));
            return true;
        }
        return false;
    }

    private static boolean isMementoOpener(String[] input) {
        if (input[1].equals("OPEN")) {
            return true;
        }
        return false;
    }

    private static boolean isMementoCloser(String[] input) {
        if (input[1].equals("CLOSE")) {
            return true;
        }
        return false;
    }

    public void timeToStop() {
        interrupt();
    }


    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BufferedWriter bufferedWriter;
    private Scanner scanner;
    private CanvasController canvasController;


}
