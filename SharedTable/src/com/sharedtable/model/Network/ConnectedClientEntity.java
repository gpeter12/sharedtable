package com.sharedtable.model.Network;

import com.sharedtable.LoggerConfig;
import com.sharedtable.controller.*;
import com.sharedtable.controller.commands.*;
import com.sharedtable.model.signals.*;
import com.sharedtable.view.MainView;
import com.sharedtable.view.MessageBox;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;

public class ConnectedClientEntity extends Thread {

    public ConnectedClientEntity(Socket socket, boolean isLowerClientEntity) {
        logger = LoggerConfig.setLogger(Logger.getLogger(MainView.class.getName()));
        logger.info("creating ConnectedClientEntity "+socket.getInetAddress()+" isLower: "+isLowerClientEntity);
        this.socket = socket;
        this.isLowerClientEntity = isLowerClientEntity;
        try {
            initializeStreams(socket);
        } catch (Exception e) {
            logger.warning("Inititalization of input/output network streams failed.");
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
            logger.warning(e.getMessage());
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
            logger.warning("ConnectedClientEntity: receivedMessage was empty!");
        }
    }

    private void handleScannerClose() {
        logger.info("handling scanner close. closing connection...");
        if(!timeToStop){
            if(amiServer()) {
                NetworkService.forwardMessageUpwards(new DisconnectSignal(networkClientEntity.getID(),
                        networkClientEntity.getNickname(),
                        networkClientEntity.getIP()).toString());
            }
        }
        if(isThreadInWait) {
            logger.info("thread in wait during shutdown process!");
            synchronized (isReadyMonitor) {
                isReadyMonitor.notifyAll();
                logger.info("threads are notifyed!");
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
            logger.info("timeToStop = true");
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
            logger.severe("send image byte array failure!");
            handleScannerClose();
        }
    }

    private void sendByteArrayUnsafe(byte[] input) throws IOException {
        logger.info("sending byte array... length: "+input.length);
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
            logger.severe("network connection dropped during byte array receive.");
            MessageBox.showError("A hálózati kapcsolat megszakadt!","A hálózati kapcsolat megszakadt \nbájttömb fogadásakor.");
        }
        return res;
    }

    private void unsafeSendPlainText(String input) {
        try {
            logger.info("sending: "+input+" to: "+id);
            bufferedWriter.write(input+"\n");
            bufferedWriter.flush();
        } catch (Exception e) {
            logger.severe("Exception happened during sending plain text: "+input+"! closing connection... " + e);
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
        logger.info("handling signal: "+signal.toString());
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
        Command receivedCommand = CommandFactory.getCommand(splittedCommand,logger);
        if(receivedCommand == null)
            return null;
        logger.info("received command: "+receivedCommand.toString()+ "from: "+id);
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
                logger.warning("illegal argument. Try receive handshaking info again...");
                return receiveNetworkClientEntityInfo();
            }
        } else {
            logger.warning("connection dropped during handshakingProcess");
            throw new RuntimeException("connection dropped during handshakingProcess");
        }
        return remoteHandshakingInfo;
    }



    private boolean amiServer() {
        return isLowerClientEntity;
    }

    private void handshakingProcess() {
        logger.info("handshaking process started");
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
        logger.info("remote ID: "+id);
        networkClientEntity = remoteHandshakingInfo;
        NetworkService.addNetworkClientEntity(remoteHandshakingInfo);

        if(amiServer()){
            if(!receiveNetworkPassword().equals(NetworkService.getNetworkPassword())){
                logger.info("passwords are not matching.");
                sendNetworkPasswordValidationResultOnSync(false);
                logger.info("sending Network Password Validation Result: false");
                handleScannerClose();
                return;
            } else {
                sendNetworkPasswordValidationResultOnSync(true);
            }

            byte[] allImageBytes = TabController.getAllBytesFromAllImages();
            sendAllImageBytesCountOnSync(allImageBytes.length);
            sendAllImageBytesOnSync(allImageBytes);
            sendSynchronizationCommandsOnHandshaking();
            unsafeSendPlainText("SYNCED");

            syncImageByteHandler = new SyncImageByteHandler(receiveAllImageBytesOnSync(receiveAllImageBytesCountOnSync()));

            receiveSynchornizationCommands();

        } else {
            sendNetworkPasswordOnSync(NetworkService.getNetworkPassword());
            if(!receiveNetworkPasswordValidationResultOnSync()) {
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
        logger.info("connections's I/O streams are initialized!");
    }

    private void sendAllImageBytesCountOnSync(int bytesCount) {
        Sleep.sleep(400,logger);
        unsafeSendPlainText(String.valueOf(bytesCount));
        logger.info("bytes count sent");
    }

    private void sendAllImageBytesOnSync(byte[] input) {
        try {
            Sleep.sleep(400,logger);
            sendByteArrayUnsafe(input);
            logger.info("all image bytes sent");
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("sendByteArrayUnsafe(input) failed");
        }
    }

    private void sendNetworkPasswordOnSync(String networkPassword) {
        unsafeSendPlainText(networkPassword);
    }

    private String receiveNetworkPassword() {
        if(scanner.hasNext())
            return scanner.nextLine();
        else {
            MessageBox.showError("A hálózati kapcsolat megszakadt!","A hálózati kapcsolat megszakadt \na jelszó fogadásakor.");
            logger.info("receiveNetworkPassword() connection closed");
            throw new RuntimeException("receiveNetworkPassword() connection closed");
        }
    }

    private boolean receiveNetworkPasswordValidationResultOnSync() {
        if(scanner.hasNext()) {
            String resultString = scanner.nextLine();
            if(resultString.equals("PASSWD_OK")){
                return true;
            } else if (resultString.equals("PASSWD_INVALID")){
                return false;
            } else {
                logger.severe("receiveNetworkPasswordValidationResult() Unrecognized result!");
                throw new RuntimeException("receiveNetworkPasswordValidationResult() Unrecognized result!");
            }
        } else {
            logger.warning(("receiveNetworkPasswordValidationResult() connection closed"));
            MessageBox.showError("A hálózati kapcsolat megszakadt!","A hálózati kapcsolat megszakadt \na jelszó validáció eredményének küldésekor.");
            throw new RuntimeException("receiveNetworkPasswordValidationResult() connection closed");
        }
    }

    private void sendNetworkPasswordValidationResultOnSync(boolean result) {
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
            logger.warning("receiveAllImageBytesCountOnSync() connection closed");
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
    private static Logger logger = null;
}
