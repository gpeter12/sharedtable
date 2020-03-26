package com.sharedtable.model;

import com.sharedtable.controller.*;
import com.sharedtable.controller.commands.ChangeStateCommand;
import com.sharedtable.controller.commands.DrawLineCommand;
import com.sharedtable.controller.controllers.CanvasController;
import com.sharedtable.model.signals.*;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectedClientEntity extends Thread {

    public ConnectedClientEntity(Socket socket, CanvasController canvasController, boolean isLowerClientEntity) {
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
        while (scanner.hasNext() && !timeToStop) {
            String receivedMessage = scanner.nextLine();
            String[] splittedMessage = receivedMessage.split(";");
            if (!receivedMessage.isEmpty()) {
                if (SignalFactory.isSignal(splittedMessage)) {
                    Signal signal = SignalFactory.getSignal(splittedMessage);
                    if(!(signal instanceof DiscoverySignal) &&
                            !(signal instanceof NewClientSignal))
                    {
                        forwardMessage(receivedMessage);
                    }
                    handleSignal(signal);
                } else {
                    forwardMessage(receivedMessage);
                    Command recvdCmd = processCommand(splittedMessage);
                    canvasController.processRemoteCommand(recvdCmd);
                }
            } else {
                System.out.println("ConnectedClientEntity: receivedMessage was empty!");
            }
        }
        if(!timeToStop){
            System.out.println("Connection closed by remote client!");
            NetworkService.forwardMessageUpwards(new DisconnectSignal(networkClientEntity.getID(),
                    networkClientEntity.getNickname(),
                    networkClientEntity.getIP()).toString());
        }
        timeToStop();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectedClientEntity that = (ConnectedClientEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    public UUID getUserId()
    {
        ;
        return id;
    }

    private void sendAllMementos() {
        ArrayList<StateMemento> mementos = canvasController.getMementos();
        for(int i=1; i<mementos.size(); i++) {
            sendMenento(mementos.get(i));
        }
    }


    public void timeToStop() {
        try {
            timeToStop = true;
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

    //<editor-fold desc="MESSAGING">

    public void sendCommand(Command command) {
        sendPlainText(command.toString());
    }


    public void sendPlainText(String input) {
        try {
            System.out.println("sending: "+input+" TO: "+id);
            bufferedWriter.write(input+"\n");
            bufferedWriter.flush();
        } catch (Exception e) {
            System.out.println("Exception happened during sending plain text! closing connection... " + e);
            timeToStop();
        }
    }

    private void sendMenento(StateMemento stateMemento) {
        ArrayList<Command> cmds = stateMemento.getCommands();
        boolean isLinked = stateMemento.getPreviousMemento() != null;
        sendMementoOpenerSignalToClient(stateMemento.getCreatorID(),stateMemento.getId(),isLinked);
        for(Command act : cmds) {
            sendCommand(act);
        }
        sendMementoCloserSignalToClient(stateMemento.getCreatorID(),stateMemento.getId(),isLinked);
    }

    private void forwardMessage(String messsage) {
        if(isLowerClientEntity){
            NetworkService.forwardMessageUpwards(messsage);
            NetworkService.forwardMessageDownwardsWithException(messsage,id);
        } else {
            NetworkService.forwardMessageDownwards(messsage);
        }
    }
    //</editor-fold> desc="MESSAGING">

    //<editor-fold desc="SIGNAL HANDLING">

    private void sendMementoOpenerSignalToClient(UUID creatorID,UUID mementoID,boolean isLinked) {
        sendPlainText(new MementoOpenerSignal(creatorID, mementoID,isLinked).toString());
    }

    private void sendMementoCloserSignalToClient(UUID creatorID,UUID mementoID,boolean isLinked) {
        sendPlainText(new MementoCloserSignal(creatorID, mementoID,isLinked).toString());
    }

    private void handleSignal(Signal signal) {
        System.out.println("signal received: "+signal.toString());
        if(signal instanceof MementoOpenerSignal) {
            RemoteDrawLineCommandBufferHandler.openNewMemento(
                    ((MementoOpenerSignal) signal).getCreatorID());
        } else if(signal instanceof MementoCloserSignal) {
            MementoCloserSignal mementoCloserSignal = (MementoCloserSignal)signal;
            RemoteDrawLineCommandBufferHandler.closeMemento(
                    mementoCloserSignal.getCreatorID(),

                    mementoCloserSignal.getMementoID(),
                    mementoCloserSignal.isLinked());
        } else if (signal instanceof NewClientSignal) {
            NewClientSignal newClientSignal = (NewClientSignal)signal;
            NetworkService.handleNewClientSignal(newClientSignal);
            NetworkService.forwardMessageUpwards(newClientSignal.toString());
        } else if(signal instanceof DisconnectSignal) {
            DisconnectSignal disconnectSignal = (DisconnectSignal)signal;
            NetworkService.handleDisconnectSignal(disconnectSignal);
        } else if(signal instanceof EntityTreeSignal) {
            EntityTreeSignal entityTreeSignal = (EntityTreeSignal) signal;
            NetworkService.handleNetworkClientEntityTreeSignal(entityTreeSignal);
        } else if(signal instanceof DiscoverySignal) {
            DiscoverySignal discoverySignal = (DiscoverySignal)signal;
            NetworkClientEntity me = NetworkService.getMyNetworkClientEntity();
            NetworkService.forwardMessageUpwards(new NewClientSignal(me.getID(),
                    me.getNickname(),
                    me.getIP(),
                    me.getPort(),
                    me.getMementoNumber(),
                    me.getUpperClientID()).toString());
            NetworkService.forwardMessageDownwards(discoverySignal.toString());
        } else if(signal instanceof PingSignal) {
            //válaszküldés
            PingSignal pingSignal = (PingSignal)signal;
            if(pingSignal.getTargetClientID().equals(UserID.getUserID()) && !pingSignal.isRespond())
                sendPingSignalResponse(pingSignal);
            if(pingSignal.isRespond() &&
                    pingSignal.getPingID().equals(currentPingIDToWaitFor))
            {
                pingFinish = System.nanoTime();
                System.out.println("ping finished");
            }
        }
    }

    private void sendPingSignalResponse(PingSignal signal) {
        signal.setRespond(true);
        System.out.println("sending ping response");
        NetworkService.sendSignalDownwards(signal);
        NetworkService.sendSignalUpwards(signal);
    }

    public void setPingIDtoWaitFor(UUID pingID) {
        currentPingIDToWaitFor = pingID;
    }

    public long getPingResult(UUID pingID) {
            if(pingID.equals(currentPingIDToWaitFor)){
                return pingFinish;
            } else {
                return -1;
            }
    }

    //</editor-fold> desc="SIGNAL HANDLING">

    //<editor-fold desc="COMMAND HANDLING">
    private Command processCommand(String[] splittedCommand) {
        Command rcvdcmd = CommandFactory.getCommand(splittedCommand, canvasController);
        if(rcvdcmd instanceof DrawLineCommand) {
            RemoteDrawLineCommandBufferHandler.addCommand(rcvdcmd);
        }
        return rcvdcmd;
    }

    //</editor-fold> desc="COMMAND HANDLING">

    //<editor-fold desc="HANDSHAKING">
    private void sendSynchronizationCommands() {
        sendAllMementos();
        sendCommand(new ChangeStateCommand(canvasController,UserID.getUserID(),canvasController.getCurrentMementoID()));
    }

    private NetworkClientEntity receiveNetworkClientEntityInfo() {
        NetworkClientEntity remoteHandshakingInfo;
        if(scanner.hasNext()) {
            remoteHandshakingInfo = new NetworkClientEntity(scanner.nextLine().split(";"));
        } else {
            throw new RuntimeException("connection dropped during handshakingProcess");
        }
        return remoteHandshakingInfo;
    }

    private void handshakingProcess() {
        NetworkClientEntity remoteHandshakingInfo = null;
        NetworkClientEntity myHandshakingInfo = NetworkService.getMyNetworkClientEntity();

        boolean imServer = isLowerClientEntity;

        if(imServer) {//I'm the server
            sendPlainText(myHandshakingInfo.toString());
            remoteHandshakingInfo = receiveNetworkClientEntityInfo();
        }
        if(!imServer) {//I'm the client
            remoteHandshakingInfo = receiveNetworkClientEntityInfo();
            myHandshakingInfo.setUpperClientID(remoteHandshakingInfo.getID());
            sendPlainText(myHandshakingInfo.toString());
        }

        id = remoteHandshakingInfo.getID();


        networkClientEntity = remoteHandshakingInfo;
        NetworkService.addNetworkClientEntity(remoteHandshakingInfo);

        if(imServer) {
            NetworkService.sendSignalUpwards(new NewClientSignal(remoteHandshakingInfo.getID(),
                    remoteHandshakingInfo.getNickname(),
                    remoteHandshakingInfo.getIP(),
                    remoteHandshakingInfo.getPort(),
                    remoteHandshakingInfo.getMementoNumber(),
                    remoteHandshakingInfo.getUpperClientID()));
        }

        NetworkService.sendDiscoverySignal();

        /*if(imServer && NetworkService.isClientInNetwork(id)){
            System.out.println("This new client is in the network! Closing connection.");
            timeToStop();
            return;
        }*/

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
            //throw new UnsupportedOperationException("mindkét kliens rendelkezik már mementókkal");
        }

    }
    //</editor-fold> desc="COMMAND HANDLING">

    private void initializeStreams(Socket socket) throws IOException {
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        scanner = new Scanner(new InputStreamReader(inputStream));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        System.out.println("connections's I/O streams are initialized!");
    }

    public NetworkClientEntity getNetworkClientEntity() {return networkClientEntity;}


    private UUID currentPingIDToWaitFor = UUID.randomUUID();
    private long pingStart;
    private long pingFinish;

    private boolean timeToStop = false;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BufferedWriter bufferedWriter;
    private Scanner scanner;
    private CanvasController canvasController;
    private boolean isLowerClientEntity;
    private UUID id = null;
    private NetworkClientEntity networkClientEntity;
}
