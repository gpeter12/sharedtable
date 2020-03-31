package com.sharedtable.model;

import com.sharedtable.controller.commands.ChangeStateCommand;
import com.sharedtable.controller.commands.CommandFactory;
import com.sharedtable.controller.StateMemento;
import com.sharedtable.controller.UserID;
import com.sharedtable.controller.commands.Command;
import com.sharedtable.controller.commands.DrawLineCommand;
import com.sharedtable.controller.CanvasController;
import com.sharedtable.controller.TabController;
import com.sharedtable.model.signals.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;

public class ConnectedClientEntity extends Thread {

    public ConnectedClientEntity(Socket socket, boolean isLowerClientEntity) {
        this.socket = socket;
        this.isLowerClientEntity = isLowerClientEntity;
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
                    TabController.getCanvasController(recvdCmd.getCanvasID()).processRemoteCommand(recvdCmd);
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
        return id;
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

    private void unsafeSendPlainText(String input) {
        try {
            System.out.println("sending: "+input+" TO: "+id);
            bufferedWriter.write(input+"\n");
            bufferedWriter.flush();
        } catch (Exception e) {
            System.out.println("Exception happened during sending plain text! closing connection... " + e);
            timeToStop();
        }
    }

    public void sendPlainText(String input) {
        if(timeToStop)
            return;
        if(!isInitialized){
            NetworkService.sleep(250);
            sendPlainText(input);
        }

        unsafeSendPlainText(input);
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

    private void sendMementoOpenerSignalToClient(UUID creatorID,UUID canvasID,UUID mementoID,boolean isLinked) {
        sendPlainText(new MementoOpenerSignal(creatorID, canvasID, mementoID,isLinked).toString());
    }

    private void sendMementoCloserSignalToClient(UUID creatorID,UUID canvasID,UUID mementoID,boolean isLinked) {
        sendPlainText(new MementoCloserSignal(creatorID, canvasID, mementoID,isLinked).toString());
    }

    private void handleSignal(Signal signal) {
        if(signal instanceof MementoOpenerSignal) {
            MementoOpenerSignal mementoOpenerSignal = (MementoOpenerSignal) signal;
            TabController.getCanvasController(mementoOpenerSignal.getCanvasID()).
                    getRemoteDrawLineCommandBufferHandler().
                    openNewMemento(
                    (mementoOpenerSignal.getCreatorID()));
        } else if(signal instanceof MementoCloserSignal) {
            MementoCloserSignal mementoCloserSignal = (MementoCloserSignal)signal;
            TabController.getCanvasController(mementoCloserSignal.getCanvasID()).
                    getRemoteDrawLineCommandBufferHandler().closeMemento(
                    mementoCloserSignal.getCreatorID(),
                    mementoCloserSignal.getMementoID(),
                    mementoCloserSignal.isLinked());
        } else if (signal instanceof NewClientSignal) {
            NewClientSignal newClientSignal = (NewClientSignal)signal;
            NetworkService.handleNewClientSignal(newClientSignal);
        } else if(signal instanceof DisconnectSignal) {
            DisconnectSignal disconnectSignal = (DisconnectSignal)signal;
            NetworkService.handleDisconnectSignal(disconnectSignal);
        } else if(signal instanceof EntityTreeSignal) {
            EntityTreeSignal entityTreeSignal = (EntityTreeSignal) signal;
            NetworkService.handleNetworkClientEntityTreeSignal(entityTreeSignal);
        } else if(signal instanceof DiscoverySignal) {
            DiscoverySignal discoverySignal = (DiscoverySignal)signal;
            NetworkService.handleDiscoverySignal(discoverySignal);
        } else if(signal instanceof PingSignal) {
            PingSignal pingSignal = (PingSignal)signal;
            handlePingSignal(pingSignal);
        } else if(signal instanceof NewTabSignal) {
            NewTabSignal newTabSignal = (NewTabSignal)signal;
            TabController.handleNewTabSingal(newTabSignal);
        } else if(signal instanceof CloseTabSignal) {
            CloseTabSignal closeTabSignal = (CloseTabSignal)signal;
            TabController.handleCloseTabSignal(closeTabSignal);
        } else if(signal instanceof SyncedSignal) {
            SyncedSignal syncedSignal = (SyncedSignal)signal;
            handleSyncedSignal(syncedSignal);
        }
    }


    private void handleSyncedSignal(SyncedSignal signal) {
        System.out.println("received Synced signal!!");
        if(signal.getCreatorID().equals(networkClientEntity.getID())){
            if(!amiServer()){
                sendSynchronizationCommandsOnHandshaking();
                System.out.println("client sent synced signal!");
                unsafeSendPlainText(new SyncedSignal(UserID.getUserID()).toString());
            }

            isInitialized = true;


            if(amiServer()) {
                NetworkService.sendSignalUpwards(new NewClientSignal(networkClientEntity.getID(),
                        networkClientEntity.getNickname(),
                        networkClientEntity.getIP(),
                        networkClientEntity.getPort(),
                        networkClientEntity.getMementoNumber(),
                        networkClientEntity.getUpperClientID()));
            }
            NetworkService.sendDiscoverySignal();
        }
    }
        //<editor-fold desc="PINGING">



        private void handlePingSignal(PingSignal pingSignal) {
            if(pingSignal.getTargetClientID().equals(UserID.getUserID()) && !pingSignal.isRespond())
                sendPingSignalResponse(pingSignal);
            if(pingSignal.isRespond() &&
                    pingSignal.getPingID().equals(currentPingIDToWaitFor))
            {
                pingFinish = System.nanoTime();
                System.out.println("ping finished");
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

        //</editor-fold> desc="PINGING">

    //</editor-fold> desc="SIGNAL HANDLING">

    //<editor-fold desc="COMMAND HANDLING">
    private Command processCommand(String[] splittedCommand) {
        Command rcvdcmd = CommandFactory.getCommand(splittedCommand);
        if(rcvdcmd instanceof DrawLineCommand) {
            TabController.getCanvasController(rcvdcmd.getCanvasID()).getRemoteDrawLineCommandBufferHandler().addCommand(rcvdcmd);
        }
        return rcvdcmd;
    }

    //</editor-fold> desc="COMMAND HANDLING">

    //<editor-fold desc="HANDSHAKING">

    private void sendMenentoOnHandshaking(StateMemento stateMemento, UUID canvasID) {
        ArrayList<Command> cmds = stateMemento.getCommands();
        boolean isLinked = stateMemento.getPreviousMemento() != null;
        unsafeSendPlainText(new MementoOpenerSignal(stateMemento.getCreatorID(),canvasID,stateMemento.getId(),isLinked).toString());
        for(Command act : cmds) {
            unsafeSendPlainText(act.toString());
        }
        unsafeSendPlainText(new MementoCloserSignal(stateMemento.getCreatorID(),canvasID,stateMemento.getId(),isLinked).toString());
    }

    private void sendAllMementosOnCanvasOnHandshaking(CanvasController canvasController) {
        ArrayList<StateMemento> mementos = canvasController.getMementos();
        for(int i=1; i<mementos.size(); i++) {
            sendMenentoOnHandshaking(mementos.get(i),canvasController.getCanvasID());
        }
    }
    private void sendSynchronizationCommandsOnHandshaking() {

        for(NewTabSignal act : TabController.generateNewTabSignalsFromAllTab()){
            unsafeSendPlainText(act.toString());
        }

        for(CanvasController act : TabController.getAllCanvasControllers()) {
            sendAllMementosOnCanvasOnHandshaking(act);
            unsafeSendPlainText(new ChangeStateCommand(act,UserID.getUserID(),act.getCurrentMementoID()).toString());
        }

    }






    private NetworkClientEntity receiveNetworkClientEntityInfo() {
        NetworkClientEntity remoteHandshakingInfo;
        if(scanner.hasNext()) {
            try {
                remoteHandshakingInfo = new NetworkClientEntity(scanner.nextLine().split(";"));
            } catch (IllegalArgumentException e){
                System.out.println("illegal argument. Try receive handshaking info again...");
                return receiveNetworkClientEntityInfo();
            }
        } else {
            throw new RuntimeException("connection dropped during handshakingProcess");
        }
        return remoteHandshakingInfo;
    }



    private boolean amiServer() {
        return isLowerClientEntity;
    }

    private void handshakingProcess() {
        NetworkClientEntity remoteHandshakingInfo = null;
        NetworkClientEntity myHandshakingInfo = NetworkService.getMyNetworkClientEntity();

        if(amiServer()) {//I'm the server
            unsafeSendPlainText(myHandshakingInfo.toString());
            remoteHandshakingInfo = receiveNetworkClientEntityInfo();

        }
        if(!amiServer()) {//I'm the client
            remoteHandshakingInfo = receiveNetworkClientEntityInfo();
            myHandshakingInfo.setUpperClientID(remoteHandshakingInfo.getID());
            unsafeSendPlainText(myHandshakingInfo.toString());
            NetworkService.setUpperClientEntity(remoteHandshakingInfo);

        }

        id = remoteHandshakingInfo.getID();


        networkClientEntity = remoteHandshakingInfo;
        NetworkService.addNetworkClientEntity(remoteHandshakingInfo);

        if(amiServer()){
            sendSynchronizationCommandsOnHandshaking();
            unsafeSendPlainText(new SyncedSignal(UserID.getUserID()).toString());
        }



        /*if(imServer && NetworkService.isClientInNetwork(id)){
            System.out.println("This new client is in the network! Closing connection.");
            timeToStop();
            return;
        }*/

        //kinek üres a memento stackje?
        /*if(myHandshakingInfo.getMementoNumber() == 1 &&
                remoteHandshakingInfo.getMementoNumber() == 1) //mindkettőnknek csak alapmementó van
        {

        } else if(myHandshakingInfo.getMementoNumber() > 1 &&
                remoteHandshakingInfo.getMementoNumber() == 1) //nekünk vannak mementóink
        {
            sendSynchronizationCommandsOnHandshaking();

        } else if(myHandshakingInfo.getMementoNumber() > 1 &&
                remoteHandshakingInfo.getMementoNumber() > 1) //mindkét kliens rendelkezik már mementókkal
        {
            sendSynchronizationCommandsOnHandshaking();
        }*/


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
    private long pingFinish;

    private boolean isInitialized = false;
    private boolean timeToStop = false;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BufferedWriter bufferedWriter;
    private Scanner scanner;
    private boolean isLowerClientEntity;
    private UUID id = null;
    private NetworkClientEntity networkClientEntity;
}
