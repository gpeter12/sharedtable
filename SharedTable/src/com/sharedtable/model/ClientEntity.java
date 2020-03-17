package com.sharedtable.model;

import com.sharedtable.controller.*;
import com.sharedtable.controller.commands.DrawLineCommand;
import com.sharedtable.controller.controllers.CanvasController;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;

public class ClientEntity extends Thread {

    public ClientEntity(Socket socket, CanvasController canvasController, boolean isLowerClientEntity) {
        this.socket = socket;
        this.isLowerClientEntity = isLowerClientEntity;
        this.canvasController = canvasController;
        try {
            initializeStreams(socket);
        } catch (Exception e) {
            throw new RuntimeException("Inititalization of input/output network streams failed.");
        }
        start();
    }

    private void sendAllMementos() {
        System.out.println("---____---------------------");
        System.out.println("SendingALLL mementos");
        ArrayList<StateMemento> mementos = canvasController.getMementos();
        for(int i=1; i<mementos.size(); i++) {
            sendMenento(mementos.get(i));
        }
        System.out.println("---____---------------------");
        System.out.println("SendingALLL mementos ENDED");
    }

    private void sendMementoOpenerSignalToClient(UUID creatorID,UUID mementoID,boolean isLinked) {
        sendPlainText(NetworkService.getMementoOpenerSignal(creatorID, mementoID,isLinked));
    }

    private void sendMementoCloserSignalToClient(UUID creatorID,UUID mementoID,boolean isLinked) {
        sendPlainText(NetworkService.getMementoCloserSignal(creatorID, mementoID,isLinked));
    }

    private void sendMenento(StateMemento stateMemento) {
        ArrayList<Command> cmds = stateMemento.getCommands();
        boolean isLinked = stateMemento.getPreviousMemento() != null;
        System.out.println("sending memento created by: "+stateMemento.getCreatorID().toString());
        sendMementoOpenerSignalToClient(stateMemento.getCreatorID(),stateMemento.getId(),isLinked);
        for(Command act : cmds) {
            sendCommand(act);
        }
        sendMementoCloserSignalToClient(stateMemento.getCreatorID(),stateMemento.getId(),isLinked);
    }

    private void sendSynchronizationCommands() {
        sendAllMementos();
    }

    private HandshakingInfo receiveHandshakingInfo() {
        HandshakingInfo remoteHandshakingInfo;
        if(scanner.hasNext()) {
            remoteHandshakingInfo = new HandshakingInfo(scanner.nextLine());
        } else {
            throw new RuntimeException("connection dropped during handshakingProcess");
        }
        return remoteHandshakingInfo;
    }

    private void handshakingProcess() {
        HandshakingInfo remoteHandshakingInfo = null;
        HandshakingInfo myHandshakingInfo = new HandshakingInfo(UserID.getUserID(),canvasController.getMementos().size());

        if(isLowerClientEntity == true) {//I'm the server
            remoteHandshakingInfo = receiveHandshakingInfo();
            sendPlainText(myHandshakingInfo.toString());
        }
        if(isLowerClientEntity == false) {//I'm the client
            sendPlainText(myHandshakingInfo.toString());
            remoteHandshakingInfo = receiveHandshakingInfo();
        }
        id = remoteHandshakingInfo.getId();
        System.out.println("client connected with "+id+" UUID");
        System.out.println("My UserID is: "+UserID.getUserID().toString());
        //kinek üres a memento stackje?
        if(myHandshakingInfo.getMementoNumber() == 1 &&
            remoteHandshakingInfo.getMementoNumber() == 1) //mindkettőnknek csak alapmementó van
        {
            return;
        } else if(myHandshakingInfo.getMementoNumber() > 1 &&
                remoteHandshakingInfo.getMementoNumber() == 1) //nekünk vannak mementóink
        {
            sendSynchronizationCommands();

        } else if(myHandshakingInfo.getMementoNumber() > 1 &&
                remoteHandshakingInfo.getMementoNumber() > 1) //mindkét kliens rendelkezik már mementókkal
        {
            throw new UnsupportedOperationException("mindkét kliens rendelkezik már mementókkal");
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientEntity that = (ClientEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void run() {
        handshakingProcess();
        while (scanner.hasNext()) {
            Command recvdCmd = receiveCommand();
            if(recvdCmd!=null)
                canvasController.processRemoteCommand(recvdCmd);
        }
        scanner.close();
        try {
            socket.close();
            NetworkService.removeClientEntity(id);
            interrupt();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public UUID getUserId() {return id;}

    private void initializeStreams(Socket socket) throws IOException {
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        scanner = new Scanner(new InputStreamReader(inputStream));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        System.out.println("connections's I/O streams are initialized!");
    }

    public void sendCommand(Command command) {
        sendPlainText(command.toString());
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
        if(isLowerClientEntity){
            NetworkService.forwardMessageUpwards(receivedCommand);
        } else {
            NetworkService.forwardMessageDownwards(receivedCommand);
        }
        String[] splittedCommand = receivedCommand.split(";");
        if (!receivedCommand.isEmpty()) {
            if(isMementoBarrierSignal(splittedCommand)){
                handleMementoBarrierSignal(splittedCommand);
                return null;
            }
            else {
                Command rcvdcmd = CommandFactory.getCommand(splittedCommand, canvasController);
                if(rcvdcmd instanceof DrawLineCommand) {
                    RemoteDrawLineCommandBufferHandler.addCommand(rcvdcmd);
                }
                return rcvdcmd;
            }
        }
        else {
            System.out.println("input command data is empty!");
            return null;
        }
    }

    private boolean isMementoBarrierSignal(String[] input) {
        if(isMementoOpener(input) || isMementoCloser(input)){
            return true;
        }
        return false;
    }

    private boolean handleMementoBarrierSignal(String[] input) {
        if(isMementoOpener(input)){
            RemoteDrawLineCommandBufferHandler.openNewMemento(UUID.fromString(input[0]));
            return true;
        }
        if(isMementoCloser(input)){
            System.out.println("memento Closer: "+input[3]);
            RemoteDrawLineCommandBufferHandler.closeMemento(UUID.fromString(input[0]),UUID.fromString(input[2]),Boolean.parseBoolean(input[3]));
            return true;
        }
        return false;
    }

    private static boolean isMementoOpener(String[] input) {
        if (input[1].equals("OPEN")) {
            System.out.println("userID: "+input[0]);
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
        try {
            socket.close();
            NetworkService.removeClientEntity(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        interrupt();
    }

    public boolean isLowerClientEntity() {
        return isLowerClientEntity;
    }

    public void setLowerClientEntity(boolean lowerClientEntity) {
        isLowerClientEntity = lowerClientEntity;
    }



    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BufferedWriter bufferedWriter;
    private Scanner scanner;
    private CanvasController canvasController;
    private boolean isLowerClientEntity;
    private UUID id = UUID.randomUUID();


}
