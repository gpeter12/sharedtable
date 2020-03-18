package com.sharedtable.model;

import com.sharedtable.controller.*;
import com.sharedtable.controller.commands.ChangeStateCommand;
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

    public boolean isLowerClientEntity() {
        return isLowerClientEntity;
    }

    public void setLowerClientEntity(boolean lowerClientEntity) {
        isLowerClientEntity = lowerClientEntity;
    }

    @Override
    public void run() {
        handshakingProcess();
        while (scanner.hasNext()) {
            String receivedMessage = scanner.nextLine();
            forwardMessage(receivedMessage);
            String[] splittedMessage = receivedMessage.split(";");
            if (!receivedMessage.isEmpty()) {
                if (isSignal(splittedMessage)) {
                    handleSignal(splittedMessage);
                } else {
                    Command recvdCmd = processCommand(splittedMessage);
                    canvasController.processRemoteCommand(recvdCmd);
                }
            } else {
                System.out.println("ClientEntity: receivedMessage was empty!");
            }
        }
        timeToStop();
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


    public UUID getUserId() {return id;}

    private void sendAllMementos() {
        ArrayList<StateMemento> mementos = canvasController.getMementos();
        for(int i=1; i<mementos.size(); i++) {
            sendMenento(mementos.get(i));
        }
    }

    public void sendCommand(Command command) {
        sendPlainText(command.toString());
    }

    public void sendPlainText(String input) {
        try {
            bufferedWriter.write(input+"\n");
            bufferedWriter.flush();
        } catch (Exception e) {
            System.out.println("Exception happened during sending plain text! closing connection... " + e);
            timeToStop();
        }
    }

    public void timeToStop() {
        try {
            bufferedWriter.close();
            outputStream.close();
            inputStream.close();
            socket.close();
            NetworkService.removeClientEntity(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        interrupt();
    }

    //---------------PRIVATE SECTION-----------------------------------------
    //------------------------------------------------------------------------


    //--------------SIGNAL HANDLING-----------------------------------------

    private String getNewClientInfoSignal(UUID clientID) {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;CONN;").append(clientID);
        return sb.toString();
    }

    private void sendMementoOpenerSignalToClient(UUID creatorID,UUID mementoID,boolean isLinked) {
        sendPlainText(NetworkService.getMementoOpenerSignal(creatorID, mementoID,isLinked));
    }

    private void sendMementoCloserSignalToClient(UUID creatorID,UUID mementoID,boolean isLinked) {
        sendPlainText(NetworkService.getMementoCloserSignal(creatorID, mementoID,isLinked));
    }

    private boolean isSignal(String[] input) {
        if(input[0].equals("SIG"))
            return true;
        return false;
    }

    private boolean isConnectionSignal(String[] input) {
        if(input[1].equals("CONN"))
            return true;
        return false;
    }

    private void handleConnectionSignal(String[] input) {
        NetworkService.addNewTransitiveClient(UUID.fromString(input[2]));
    }

    private void handleSignal(String[] input) {
        if(isMementoBarrierSignal(input))
            handleMementoBarrierSignal(input);
        else if(isConnectionSignal(input))
            handleConnectionSignal(input);
    }

    private boolean isMementoBarrierSignal(String[] input) {
        if(isMementoOpenerSignal(input) || isMementoCloserSignal(input)){
            return true;
        }
        return false;
    }

    private boolean handleMementoBarrierSignal(String[] input) {
        if(isMementoOpenerSignal(input)){
            RemoteDrawLineCommandBufferHandler.openNewMemento(UUID.fromString(input[1]));
            return true;
        }
        if(isMementoCloserSignal(input)){
            RemoteDrawLineCommandBufferHandler.closeMemento(UUID.fromString(input[1]),UUID.fromString(input[3]),Boolean.parseBoolean(input[4]));
            return true;
        }
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

    //--------------END OF SIGNAL HANDLING-----------------------------------------


    //--------------COMMAND HANDLING-----------------------------------------

    private Command processCommand(String[] splittedCommand) {

        Command rcvdcmd = CommandFactory.getCommand(splittedCommand, canvasController);
        if(rcvdcmd instanceof DrawLineCommand) {
            RemoteDrawLineCommandBufferHandler.addCommand(rcvdcmd);
        }
        return rcvdcmd;
    }


    //--------------END OF COMMAND HANDLING-----------------------------------------


    //--------------HANDSHAKING-----------------------------------------

    private void sendSynchronizationCommands() {
        sendAllMementos();
        sendCommand(new ChangeStateCommand(canvasController,UserID.getUserID(),canvasController.getCurrentMementoID()));
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

    private void propagateNewClientInfo(UUID clientID) {
        NetworkService.forwardMesageDownwardsWithException(getNewClientInfoSignal(clientID), clientID);
        NetworkService.forwardMessageUpwards(getNewClientInfoSignal(clientID));
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
        if(NetworkService.isClientInNetwork(id)){
            System.out.println("This new client is in the network! Closing connection.");
            timeToStop();
        }

        propagateNewClientInfo(id);

        //kinek üres a memento stackje?
        if(myHandshakingInfo.getMementoNumber() == 1 &&
                remoteHandshakingInfo.getMementoNumber() == 1) //mindkettőnknek csak alapmementó van
        {

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

    //--------------END OF HANDSHAKING-----------------------------------------

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

    private void forwardMessage(String messsage) {
        if(isLowerClientEntity){
            NetworkService.forwardMessageUpwards(messsage);
            NetworkService.forwardMesageDownwardsWithException(messsage,id);
        } else {
            NetworkService.forwardMessageDownwards(messsage);
        }
    }

    private void initializeStreams(Socket socket) throws IOException {
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        scanner = new Scanner(new InputStreamReader(inputStream));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        System.out.println("connections's I/O streams are initialized!");
    }





    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BufferedWriter bufferedWriter;
    private Scanner scanner;
    private CanvasController canvasController;
    private boolean isLowerClientEntity;
    private UUID id = UUID.randomUUID();
    private String IP;

}
