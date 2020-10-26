package com.sharedtable.model.network;

import com.sharedtable.Constants;
import com.sharedtable.Utils;
import com.sharedtable.controller.*;
import com.sharedtable.controller.commands.*;
import com.sharedtable.model.network.signals.*;
import com.sharedtable.controller.MainViewController;
import com.sharedtable.view.MessageBox;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

public class ConnectedClientEntity extends Thread {

    private UUID currentPingIDToWaitFor = UUID.randomUUID();
    private long pingFinish;
    //Szinrkonizációnál nagyon fontos a lock, mert előfordulhat hogy olyan memetóra történik hivatkozás ami még nincs a becsatlakozó kliensénél
    private Semaphore sendLockSemaphore = new Semaphore(1);
    private boolean timeToStop = false;
    private Socket socket;
    private Socket byteReceiverSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BufferedWriter bufferedWriter;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Scanner scanner;
    private boolean isLowerClientEntity;
    private UUID id = null;
    private NetworkClientEntity networkClientEntity;
    private Logger logger;
    private boolean isSyncFinished;

    public ConnectedClientEntity(Socket socket, Socket brSocket, boolean isLowerClientEntity) {
        logger = Logger.getLogger(MainViewController.class.getName());
        logger.info("creating ConnectedClientEntity isLower: "+isLowerClientEntity);
        this.socket = socket;
        this.byteReceiverSocket = brSocket;
        this.isLowerClientEntity = isLowerClientEntity;
        try {
            initializeStreams(socket,brSocket);
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
            e.printStackTrace();
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
                else {
                    if(receivedCommand instanceof DrawImageCommand) {
                        forwardDrawImageCommand((DrawImageCommand)receivedCommand);
                    } else {
                        forwardMessage(message);
                    }
                }
                TabController.getInstance().getCanvasController(receivedCommand.getCanvasID()).processRemoteCommand(receivedCommand);
            }
        } else {
            logger.warning("ConnectedClientEntity: receivedMessage was empty!");
        }
    }

    public void handleScannerClose() {
        logger.info("handling scanner close. closing connection...");
        if(!timeToStop){
            if(amiServer()) {
                NetworkService.getInstance().forwardMessageUpwards(new DisconnectSignal(networkClientEntity.getID(),
                        networkClientEntity.getNickname()).toString());
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
            byteReceiverSocket.close();
            NetworkService.getInstance().removeClientEntity(id);
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
        try { sendLockSemaphore.acquire(); } catch (InterruptedException e) { e.printStackTrace(); }
        unsafeSendByteArray(input);
        sendLockSemaphore.release();
    }

    private void unsafeSendByteArray(byte[] input) {
        logger.info("sending byte array... length: "+input.length);
        try {
            dataOutputStream.write(input);
            dataOutputStream.flush();
        } catch (IOException e) {
            logger.severe("image byte sending failed");
            handleScannerClose();
        }

    }



    private byte[] receiveByteArray(int length) {
        byte[] res = new byte[length];
        System.out.println("reading "+length+" bytes...");
        try {
            //while(dataInputStream.readByte() == 0) {}
            dataInputStream.readFully(res);
        } catch (IOException e) {
            e.printStackTrace();
            handleScannerClose();
            logger.severe("network connection dropped during byte array receive.");
            MessageBox.showError("A hálózati kapcsolat megszakadt!","A hálózati kapcsolat megszakadt \nbájt tömb fogadásakor.");
        }
        System.out.println(length+" bytes read!");
        return res;
    }

    private void printStackElementArray(StackTraceElement[] elements){
        for(StackTraceElement act : elements) {
            System.out.println(act.toString()+"\n");
        }

    }

    private void unsafeSendPlainText(String input) {
        try {
            //System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
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
        try { sendLockSemaphore.acquire(); } catch (InterruptedException e) { e.printStackTrace(); }
        unsafeSendPlainText(input);
        sendLockSemaphore.release();
    }

    private void forwardMessage(String messsage) {
        if(isLowerClientEntity){
            NetworkService.getInstance().forwardMessageUpwards(messsage);
            NetworkService.getInstance().forwardMessageDownwardsWithException(messsage,id);
        } else {
            NetworkService.getInstance().forwardMessageDownwards(messsage);
        }
    }

    private void forwardDrawImageCommand(DrawImageCommand command) {
        if(isLowerClientEntity){
            NetworkService.getInstance().forwardDrawImageCommandUpwards(command);
            NetworkService.getInstance().forwardDrawImageCommandDownwardsWithException(command,id);
        } else {
            NetworkService.getInstance().forwardDrawImageCommandDownwards(command);
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
            NetworkService.getInstance().handleNewClientSignal(newClientSignal);
        } else if(signal instanceof DisconnectSignal) {
            DisconnectSignal disconnectSignal = (DisconnectSignal)signal;
            NetworkService.getInstance().handleDisconnectSignal(disconnectSignal);
        } else if(signal instanceof RenameTabSignal) {
            RenameTabSignal renameTabSignal = (RenameTabSignal)signal;
            handleRenameTabSignal(renameTabSignal);
        } else if(signal instanceof EntityTreeSignal) {
            EntityTreeSignal entityTreeSignal = (EntityTreeSignal) signal;
            NetworkService.getInstance().handleNetworkClientEntityTreeSignal(entityTreeSignal);
        } else if(signal instanceof DiscoverySignal) {
            DiscoverySignal discoverySignal = (DiscoverySignal)signal;
            NetworkService.getInstance().handleDiscoverySignal(discoverySignal);
        } else if(signal instanceof PingSignal) {
            PingSignal pingSignal = (PingSignal)signal;
            handlePingSignal(pingSignal);
        } else if(signal instanceof NewTabSignal) {
            NewTabSignal newTabSignal = (NewTabSignal)signal;
            TabController.getInstance().handleNewTabSingal(newTabSignal);
        } else if(signal instanceof CloseTabSignal) {
            CloseTabSignal closeTabSignal = (CloseTabSignal)signal;
            TabController.getInstance().handleCloseTabSignal(closeTabSignal);
        } else if(signal instanceof ChatMessageSignal) {
            ChatMessageSignal chatMessageSignal = (ChatMessageSignal)signal;
            NetworkService.getInstance().handleChatMessageSignal(chatMessageSignal);
        } else if(signal instanceof NetworkPasswordChangeSignal) {
            NetworkPasswordChangeSignal networkPasswordChangeSignal = (NetworkPasswordChangeSignal)signal;
            handleChangeNetworkPasswordSignal(networkPasswordChangeSignal);
        } else if(signal instanceof DeleteAfterSignal) {
            DeleteAfterSignal deleteAfterSignal = (DeleteAfterSignal)signal;
            handleDeleteAfterSignal(deleteAfterSignal);
        }
    }

    private void handleDeleteAfterSignal(DeleteAfterSignal deleteAfterSignal) {
        TabController.getInstance().getCanvasController(deleteAfterSignal.getCanvasID()).
                deleteAllMementoAfter(deleteAfterSignal.getMementoID());
    }

    private void handleRenameTabSignal(RenameTabSignal renameTabSignal) {
        TabController.getInstance().renameTab(renameTabSignal.getCanvasID(),renameTabSignal.getTabName());
    }

    private void handleChangeNetworkPasswordSignal(NetworkPasswordChangeSignal networkPasswordChangeSignal) {
        NetworkService.getInstance().setNetworkPassword(networkPasswordChangeSignal.getPassword());
    }

    private void handleMementoCloserSignal(MementoCloserSignal signal) {
        TabController.getInstance().getCanvasController(signal.getCanvasID()).
                getRemoteDrawLineCommandBufferHandler().closeMemento(
                signal.getCreatorID(),
                signal.getMementoID(),
                signal.isLinked(),
                signal.getPrevMementoID(),
                signal.getNextMementoID());
    }

    private void handleMementoOpenerSingnal(MementoOpenerSignal signal) {
        TabController.getInstance().getCanvasController(signal.getCanvasID()).
                getRemoteDrawLineCommandBufferHandler().
                openNewMemento(
                        (signal.getCreatorID()));
    }

    //<editor-fold desc="PINGING">

    private void handlePingSignal(PingSignal pingSignal) {
        if(pingSignal.getTargetClientID().equals(UserID.getInstance().getUserID()) && !pingSignal.isResponse())
            sendPingSignalResponse(pingSignal);
        if(pingSignal.isResponse() &&
                pingSignal.getPingID().equals(currentPingIDToWaitFor))
        {
            pingFinish = System.nanoTime();
        }
    }

    private void sendPingSignalResponse(PingSignal signal) {
        signal.setResponse(true);
        NetworkService.getInstance().sendSignalDownwards(signal);
        NetworkService.getInstance().sendSignalUpwards(signal);
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
        if(receivedCommand == null) {
            logger.warning("unrecognized command: "+ Utils.recombineStringArray(splittedCommand));
            return null;
        }

        logger.info("received command: "+receivedCommand.toString()+ " from: "+id);
        if(receivedCommand instanceof DrawLineCommand) {
            TabController.getInstance().getCanvasController(receivedCommand.getCanvasID()).getRemoteDrawLineCommandBufferHandler().addCommand(receivedCommand);
        }
        else if(receivedCommand instanceof DrawImageCommand) {
            DrawImageCommand drawImageCommand = (DrawImageCommand) receivedCommand;
            drawImageCommand.setImage(receiveByteArray(drawImageCommand.getImageSize()));
            TabController.getInstance().getCanvasController(receivedCommand.getCanvasID()).getRemoteDrawLineCommandBufferHandler().addCommand(receivedCommand);
        }
        else if(receivedCommand instanceof DrawRectangleCommand) { //és mindenki aki belőle származik
            TabController.getInstance().getCanvasController(receivedCommand.getCanvasID()).getRemoteDrawLineCommandBufferHandler().addCommand(receivedCommand);
        }
        return receivedCommand;
    }

    public synchronized void sendDrawImageCommand(DrawImageCommand command) {
        command.setImageSize(command.getImageBytes().length);
        sendPlainText(command.toString());
        sendByteArray(command.getImageBytes());
    }

    //</editor-fold> desc="COMMAND HANDLING">

    //<editor-fold desc="HANDSHAKING">

    private void sendMementoOnHandshaking(StateMemento stateMemento, UUID canvasID) {
        ArrayList<Command> cmds = stateMemento.getCommands();
        boolean isLinked = stateMemento.getPreviousMemento() != null;
        unsafeSendPlainText(new MementoOpenerSignal(stateMemento.getCreatorID(),canvasID).toString());
        for(Command act : cmds) {
            unsafeSendPlainText(act.toString());
            if(act instanceof DrawImageCommand) {
                unsafeSendByteArray(((DrawImageCommand) act).getImageBytes());
            }
        }
        unsafeSendPlainText(new MementoCloserSignal(stateMemento.getCreatorID(),canvasID,stateMemento.getId(),isLinked,
                stateMemento.getPreviousMementoID(),stateMemento.getNextMementoID()).toString());
    }

    private void sendAllMementosOnCanvasOnHandshaking(CanvasController canvasController) {
        ArrayList<StateMemento> mementos = canvasController.getMementos();
        for(int i=1; i<mementos.size(); i++) {
            sendMementoOnHandshaking(mementos.get(i),canvasController.getCanvasID());
        }
    }
    private void sendSynchronizationCommandsOnHandshaking() {

        for(NewTabSignal act : TabController.getInstance().generateNewTabSignalsFromAllTab()){
            unsafeSendPlainText(act.toString());
        }

        for(CanvasController act : TabController.getInstance().getAllCanvasControllers()) {
            sendAllMementosOnCanvasOnHandshaking(act);
            unsafeSendPlainText(new ChangeStateCommand(act,UserID.getInstance().getUserID(),act.getCurrentMementoID()).toString());
        }

        for(ChatMessageSignal act : ChatService.getInstance().getAllChatMessageSignal()) {
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

    private synchronized void handshakingProcess() {
        logger.info("handshaking process started");
        try { sendLockSemaphore.acquire(); } catch (InterruptedException e) { e.printStackTrace(); }
        MessageBox.showSyncWindow();

        NetworkClientEntity remoteHandshakingInfo = null;
        NetworkClientEntity myHandshakingInfo = NetworkService.getInstance().getMyNetworkClientEntity();

        if(amiServer()) {//I'm the server
            unsafeSendPlainText(myHandshakingInfo.toString());
            remoteHandshakingInfo = receiveNetworkClientEntityInfo();

        }
        if(!amiServer()) {//I'm the client
            remoteHandshakingInfo = receiveNetworkClientEntityInfo();
            myHandshakingInfo.setUpperClientID(remoteHandshakingInfo.getID());
            unsafeSendPlainText(myHandshakingInfo.toString());
            NetworkService.getInstance().setUpperClientEntity(remoteHandshakingInfo);

        }

        id = remoteHandshakingInfo.getID();
        logger.info("remote ID: "+id);
        networkClientEntity = remoteHandshakingInfo;



        NetworkService.getInstance().addNetworkClientEntity(remoteHandshakingInfo);

        if(amiServer()){
            if(!receiveNetworkPassword().equals(NetworkService.getInstance().getNetworkPassword())){
                logger.info("passwords are not matching.");
                sendNetworkPasswordValidationResultOnSync(false);
                logger.info("sending Network Password Validation Result: false");
                handleScannerClose();
                return;
            } else {
                sendNetworkPasswordValidationResultOnSync(true);
            }
            
            sendSynchronizationCommandsOnHandshaking();
            unsafeSendPlainText("SYNCED");
            receiveSynchornizationCommands();
        } else {
            sendNetworkPasswordOnSync(NetworkService.getInstance().getNetworkPassword());
            if(!receiveNetworkPasswordValidationResultOnSync()) {
                MessageBox.showError("Érvénytelen jelszó!", "A megadott hálózati jelszó érvénytelen");
                handleScannerClose();
                return;
            }

            receiveSynchornizationCommands();
            sendSynchronizationCommandsOnHandshaking();
            unsafeSendPlainText("SYNCED");
        }
        sendLockSemaphore.release();
        isSyncFinished = true;
        if(amiServer()) {
            NetworkService.getInstance().sendSignalUpwards(new NewClientSignal(networkClientEntity.getID(),
                    networkClientEntity.getNickname(),
                    networkClientEntity.getIP(),
                    networkClientEntity.getPort(),
                    networkClientEntity.getMementoNumber(),
                    networkClientEntity.getUpperClientID(),
                    networkClientEntity.getClientBuildNumber()));
        }
        NetworkService.getInstance().sendDiscoverySignal();
        MessageBox.closeSyncWindow();
        VersionMismatchHandler.HandleVersionMismatch(checkClientVersion(remoteHandshakingInfo));
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


    //</editor-fold>

    private void initializeStreams(Socket socket,Socket brSocket) throws IOException {
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        scanner = new Scanner(new InputStreamReader(new BufferedInputStream(inputStream,512*1024)));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream),512*1024);
        dataInputStream = new DataInputStream(new BufferedInputStream(byteReceiverSocket.getInputStream(),1024*1024*4));
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteReceiverSocket.getOutputStream(),1024*1024*4));

        logger.info("connections's I/O streams are initialized!");
    }

    private int checkClientVersion(NetworkClientEntity entity) {
        return Integer.compare(entity.getClientBuildNumber(), Constants.getBuildNumber());
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
            if(resultString.equals(Constants.getNetworkPasswordValidationOK())){
                return true;
            } else if (resultString.equals(Constants.getNetworkPasswordValidationINVALID())){
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
            unsafeSendPlainText(Constants.getNetworkPasswordValidationOK());
        } else {
            unsafeSendPlainText(Constants.getNetworkPasswordValidationINVALID());
        }
    }


}
