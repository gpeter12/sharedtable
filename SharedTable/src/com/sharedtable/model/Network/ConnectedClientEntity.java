package com.sharedtable.model.Network;

import com.sharedtable.controller.*;
import com.sharedtable.controller.commands.*;
import com.sharedtable.model.signals.*;
import com.sharedtable.view.MessageBox;

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

    public NetworkClientEntity getNetworkClientEntity() {return networkClientEntity;}

    @Override
    public void run() {
        setName("clientEntityMessageReceiver");
        try {
            handshakingProcess();
        } catch (Exception e) {
            e.printStackTrace();
            handleScannerClose();
            return;
        }

        while (scanner.hasNext() && !timeToStop) {
            String receivedMessage = scanner.nextLine();
            processMessage(receivedMessage);
        }
        handleScannerClose();
    }

    private void processMessage(String message) {
        String[] splittedMessage = message.split(";");
        if (!message.isEmpty()) {
            if (SignalFactory.isSignal(splittedMessage)) {
                Signal signal = SignalFactory.getSignal(splittedMessage);
                forwardMessage(signal.toString());
                handleSignal(signal);
            } else { //akkor command
                Command receivedCommand = processCommand(splittedMessage);
                if(receivedCommand == null)
                    return;
                if(receivedCommand instanceof DrawImageCommand) {
                    forwardDrawImageCommand((DrawImageCommand)receivedCommand);
                } else {
                    forwardMessage(message);
                }
                TabController.getCanvasController(receivedCommand.getCanvasID()).processRemoteCommand(receivedCommand);
            }
        } else {
            System.out.println("ConnectedClientEntity: receivedMessage was empty!");
        }
    }

    private void handleScannerClose() {
        if(!timeToStop){
            if(amiServer()) {
                NetworkService.forwardMessageUpwards(new DisconnectSignal(networkClientEntity.getID(),
                        networkClientEntity.getNickname(),
                        networkClientEntity.getIP()).toString());
            }
        }
        if(isThreadInWait) {
            synchronized (isReadyMonitor) {
                isReadyMonitor.notifyAll();
            }
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
            setClientEntityReady();
        } catch (IOException e) {
            e.printStackTrace();
        }
        interrupt();
    }

    //<editor-fold desc="MESSAGING">

    public void sendCommand(Command command) {
        sendPlainText(command.toString());
    }

    public void sendByteArray(byte[] input) {
        if(timeToStop)
            return;
        try {
            sendByteArrayUnsafe(input);
        } catch (IOException e) {
            System.out.println("send image byte array failure!");
            handleScannerClose();
        }
    }

    private void sendByteArrayUnsafe(byte[] input) throws IOException {
        dataOutputStream.write(input);
        dataOutputStream.flush();
    }



    private byte[] receiveByteArray(int length) {
        byte[] res = new byte[length];
        try {
            //while(dataInputStream.readByte() == 0) {}
            dataInputStream.readFully(res);
        } catch (IOException e) {
            e.printStackTrace();
            handleScannerClose();
            handleScannerClose();
            MessageBox.showError("A hálózati kapcsolat megszakadt!","A hálózati kapcsolat megszakadt \nbájttömb fogadásakor.");
        }
        return res;
    }

    private void unsafeSendPlainText(String input) {
        try {
            System.out.println("sending: "+input+" to: "+id);
            bufferedWriter.write(input+"\n");
            bufferedWriter.flush();
        } catch (Exception e) {
            System.out.println("Exception happened during sending plain text: "+input+"! closing connection... " + e);
            handleScannerClose();
        }
    }

    public void sendPlainText(String input) {
        if(timeToStop)
            return;
        waitUntilNotify();
        if(timeToStop)
            return;
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

    private void forwardDrawImageCommand(DrawImageCommand command) {
        if(isLowerClientEntity){
            NetworkService.forwardDrawImageCommandUpwards(command);
            NetworkService.forwardDrawImageCommandDownwardsWithException(command,id);
        } else {
            NetworkService.forwardDrawImageCommandDownwards(command);
        }
    }


    //</editor-fold> desc="MESSAGING">

    //<editor-fold desc="SIGNAL HANDLING">

    private void handleSignal(Signal signal) {
        if(signal instanceof MementoOpenerSignal) {
            MementoOpenerSignal mementoOpenerSignal = (MementoOpenerSignal) signal;
            handleMementoOpenerSingnal(mementoOpenerSignal);
        } else if(signal instanceof MementoCloserSignal) {
            MementoCloserSignal mementoCloserSignal = (MementoCloserSignal)signal;
            handleMementoCloserSignal(mementoCloserSignal);
        } else if (signal instanceof NewClientSignal) {
            NewClientSignal newClientSignal = (NewClientSignal)signal;
            NetworkService.handleNewClientSignal(newClientSignal);
        } else if(signal instanceof DisconnectSignal) {
            DisconnectSignal disconnectSignal = (DisconnectSignal)signal;
            NetworkService.handleDisconnectSignal(disconnectSignal);
        } else if(signal instanceof RenameTabSignal) {
            RenameTabSignal renameTabSignal = (RenameTabSignal)signal;
            handleRenameTabSignal(renameTabSignal);
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
        } else if(signal instanceof ChatMessageSignal) {
            ChatMessageSignal chatMessageSignal = (ChatMessageSignal)signal;
            NetworkService.handleChatMessageSignal(chatMessageSignal);
        } else if(signal instanceof ByteReceiveReadySignal) {
            ByteReceiveReadySignal byteReceiveReadySignal = (ByteReceiveReadySignal)signal;
            //handleByteReceiveReadySignal(byteReceiveReadySignal);
        } else if(signal instanceof NetworkPasswordChangeSignal) {
            NetworkPasswordChangeSignal networkPasswordChangeSignal = (NetworkPasswordChangeSignal)signal;
            handleChangeNetworkPasswordSignal(networkPasswordChangeSignal);
        }
    }

    private void handleRenameTabSignal(RenameTabSignal renameTabSignal) {
        TabController.renameTab(renameTabSignal.getCanvasID(),renameTabSignal.getTabName());
    }

    private void handleChangeNetworkPasswordSignal(NetworkPasswordChangeSignal networkPasswordChangeSignal) {
        NetworkService.setNetworkPassword(networkPasswordChangeSignal.getPassword());
    }

    private void handleByteReceiveReadySignal(ByteReceiveReadySignal signal) {
        if(UserID.getUserID().equals(signal.getReceiverID())) {
            //setImageSendLockReady();
        }
    }

    private void handleMementoCloserSignal(MementoCloserSignal signal) {
        TabController.getCanvasController(signal.getCanvasID()).
                getRemoteDrawLineCommandBufferHandler().closeMemento(
                signal.getCreatorID(),
                signal.getMementoID(),
                signal.isLinked());
    }

    private void handleMementoOpenerSingnal(MementoOpenerSignal signal) {
        TabController.getCanvasController(signal.getCanvasID()).
                getRemoteDrawLineCommandBufferHandler().
                openNewMemento(
                        (signal.getCreatorID()));
    }
        //<editor-fold desc="PINGING">

        private void handlePingSignal(PingSignal pingSignal) {
            if(pingSignal.getTargetClientID().equals(UserID.getUserID()) && !pingSignal.isRespond())
                sendPingSignalResponse(pingSignal);
            if(pingSignal.isRespond() &&
                    pingSignal.getPingID().equals(currentPingIDToWaitFor))
            {
                pingFinish = System.nanoTime();
            }
        }

        private void sendPingSignalResponse(PingSignal signal) {
            signal.setRespond(true);
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
        Command receivedCommand = CommandFactory.getCommand(splittedCommand);
        if(receivedCommand == null)
            return null;
        System.out.println("received command: "+receivedCommand.toString()+ "from: "+id);
        if(receivedCommand instanceof DrawLineCommand) {
            TabController.getCanvasController(receivedCommand.getCanvasID()).getRemoteDrawLineCommandBufferHandler().addCommand(receivedCommand);
        } else if(receivedCommand instanceof DrawImageCommand) {
            DrawImageCommand drawImageCommand = (DrawImageCommand)receivedCommand;
            if(isSyncFinished) {
                drawImageCommand.setImage(receiveByteArray(drawImageCommand.getImageSize()));
            } else {
                drawImageCommand.setImage(syncImageByteHandler.getNextNBytes(drawImageCommand.getImageSize()));
            }
            TabController.getCanvasController(receivedCommand.getCanvasID()).getRemoteDrawLineCommandBufferHandler().addCommand(receivedCommand);
        }
        else if(receivedCommand instanceof DrawRectangleCommand) { //és mindenki aki belőle származik
            TabController.getCanvasController(receivedCommand.getCanvasID()).getRemoteDrawLineCommandBufferHandler().addCommand(receivedCommand);
        }
        return receivedCommand;
    }

    public void sendDrawImageCommand(DrawImageCommand command) {
        command.setImageSize(command.getImageBytes().length);
        sendPlainText(command.toString());
        isReadyToSendTo = false;
        sendByteArray(command.getImageBytes());
        setClientEntityReady();
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

        for(ChatMessageSignal act : ChatService.getAllChatMessageSignal()) {
            unsafeSendPlainText(act.toString());
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
            if(!receiveNetworkPassword().equals(NetworkService.getNetworkPassword())){
                sendNetworkPasswordValidationResult(false);
                handleScannerClose();
                return;
            } else {
                sendNetworkPasswordValidationResult(true);
            }

            byte[] allImageBytes = TabController.getAllBytesFromAllImages();
            sendAllImageBytesCountOnSync(allImageBytes.length);
            sendAllImageBytesOnSync(allImageBytes);
            sendSynchronizationCommandsOnHandshaking();
            unsafeSendPlainText("SYNCED");

            syncImageByteHandler = new SyncImageByteHandler(receiveAllImageBytesOnSync(receiveAllImageBytesCountOnSync()));

            receiveSynchornizationCommands();

        } else {
            sendNetworkPassword(NetworkService.getNetworkPassword());
            if(!receiveNetworkPasswordValidationResult()) {
                MessageBox.showError("Érvénytelen jelszó!", "A megadott hálózati jelszó érvénytelen");
                handleScannerClose();
                return;
            }

            syncImageByteHandler = new SyncImageByteHandler(receiveAllImageBytesOnSync(receiveAllImageBytesCountOnSync()));
            receiveSynchornizationCommands();

            byte[] allImageBytes = TabController.getAllBytesFromAllImages();
            sendAllImageBytesCountOnSync(allImageBytes.length);
            sendAllImageBytesOnSync(allImageBytes);
            sendSynchronizationCommandsOnHandshaking();
            unsafeSendPlainText("SYNCED");
        }

        setClientEntityReady();
        isSyncFinished = true;
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
    //</editor-fold> desc="COMMAND HANDLING">

    //<editor-fold desc = "lockHandling">

    private void receiveSynchornizationCommands() {
        while (scanner.hasNext()) {
            String receivedMessage = scanner.nextLine();
            if(receivedMessage.equals("SYNCED"))
                break;
            processMessage(receivedMessage);
        }
    }

    private void setClientEntityReady() {
        isReadyToSendTo = true;
        if(isThreadInWait) {
            synchronized (isReadyMonitor) {
                isReadyMonitor.notifyAll();
            }
        }
        isThreadInWait = false;
    }

    private void waitUntilNotify() {

        synchronized (isReadyMonitor) {
            while(!isReadyToSendTo){
                isThreadInWait = true;
                try {
                    isReadyMonitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                isThreadInWait = false;
            }
        }

    }

    //</editor-fold>

    private void initializeStreams(Socket socket) throws IOException {
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        scanner = new Scanner(new InputStreamReader(new BufferedInputStream(inputStream)));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        dataInputStream = new DataInputStream(new BufferedInputStream(inputStream));
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(outputStream));
        //dataInputStream.
        System.out.println("connections's I/O streams are initialized!");
    }

    private void sendAllImageBytesCountOnSync(int bytesCount) {
        Sleep.sleep(400);
        unsafeSendPlainText(String.valueOf(bytesCount));
        System.out.println("bytes count sent");
    }

    private void sendAllImageBytesOnSync(byte[] input) {
        try {
            Sleep.sleep(400);
            sendByteArrayUnsafe(input);
            System.out.println("all image bytes sent");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("sendByteArrayUnsafe(input) failed");
        }
    }

    private void sendNetworkPassword(String networkPassword) {
        unsafeSendPlainText(networkPassword);
    }

    private String receiveNetworkPassword() {
        if(scanner.hasNext())
            return scanner.nextLine();
        else {
            MessageBox.showError("A hálózati kapcsolat megszakadt!","A hálózati kapcsolat megszakadt \na jelszó fogadásakor.");
            throw new RuntimeException("receiveNetworkPassword() connection closed");
        }
    }

    private boolean receiveNetworkPasswordValidationResult() {
        if(scanner.hasNext()) {
            String resultString = scanner.nextLine();
            if(resultString.equals("PASSWD_OK")){
                return true;
            } else if (resultString.equals("PASSWD_INVALID")){
                return false;
            } else {
                throw new RuntimeException("receiveNetworkPasswordValidationResult() Unrecognized result!");
            }
        } else {
            MessageBox.showError("A hálózati kapcsolat megszakadt!","A hálózati kapcsolat megszakadt \na jelszó validáció eredményének küldésekor.");
            throw new RuntimeException("receiveNetworkPasswordValidationResult() connection closed");
        }
    }

    private void sendNetworkPasswordValidationResult(boolean result) {
        if(result){
            unsafeSendPlainText("PASSWD_OK");
        } else {
            unsafeSendPlainText("PASSWD_INVALID");
        }
    }

    private int receiveAllImageBytesCountOnSync() {
        if(scanner.hasNext())
            return Integer.parseInt(scanner.nextLine());
        else {
            MessageBox.showError("A hálózati kapcsolat megszakadt!","A hálózati kapcsolat megszakadt \na képek méretének fogadásakor.");
            throw new RuntimeException("receiveAllImageBytesCountOnSync() connection closed");
        }

    }

    private byte[] receiveAllImageBytesOnSync(int length) {
        return receiveByteArray(length);
    }

    private SyncImageByteHandler syncImageByteHandler;
    private UUID currentPingIDToWaitFor = UUID.randomUUID();
    private long pingFinish;
    private boolean isReadyToSendTo = false;
    private boolean isSyncFinished = false;
    //Szinrkonizációnál nagyon fontos a lock, mert előfordulhat hogy olyan memetóra történik hivatkozás ami még nincs a becsatlakozó kliensénél
    private Object isReadyMonitor = new Object();
    private boolean isThreadInWait = false;
    private boolean timeToStop = false;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BufferedWriter bufferedWriter;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Scanner scanner;
    private boolean isLowerClientEntity;
    private UUID id = null;
    private NetworkClientEntity networkClientEntity;
}
