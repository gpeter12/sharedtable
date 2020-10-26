import com.sharedtable.Constants;
import com.sharedtable.controller.*;
import com.sharedtable.controller.commands.ClearCommand;
import com.sharedtable.model.network.NetworkClientEntityTree;
import com.sharedtable.model.network.NetworkService;
import com.sharedtable.controller.MainViewController;
import com.sharedtable.view.STCanvas;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CanvasControllerTest {

    private NetworkService networkService;
    private NetworkClientEntityTree entityTree;
    private UserID userID = UserID.getInstance();
    private CanvasController canvasController;
    private StateCaretaker stateCaretaker;
    private ArrayList<StateMemento> testList = new ArrayList<>();
    private UUID creatorID = UserID.getInstance().getUserID();
    private StateOriginator stateOriginator;
    private Logger logger = Logger.getLogger(MainViewController.class.getName());


    public CanvasControllerTest() {
        userID.initWithoutPersistence(UUID.fromString("1c183421-b375-4899-85e2-50bd25ddba8f"),
                "testUser","0.0.0.0");
        networkService = NetworkService.getInstance();
        networkService.initService();
        entityTree = networkService.getEntityTree();
        networkService.switchToTestMode();
        canvasController = new CanvasController(new STCanvas(),UUID.randomUUID());
        stateOriginator = canvasController.getStateOriginator();
        stateCaretaker = canvasController.getStateCaretaker();

    }

    private StateMemento makeFirstMemento() {
        StateMemento firstMemento = stateOriginator.createMemento();
        firstMemento.setId(Constants.getNilUUID());
        firstMemento.setPreviousMementoID(Constants.getNilUUID());
        firstMemento.setNextMementoID(Constants.getEndChainUUID());
        return firstMemento;
    }

    private void prepareStateCaretakerAndTestList() throws StateChainInconsistencyException {
        stateOriginator = new StateOriginator();
        stateCaretaker.clearStateChain();

        StateMemento firstMemento = makeFirstMemento();
        stateCaretaker.addFirstMemento(firstMemento);

        testList.add(firstMemento);//0
        testList.add(new StateMemento(UUID.fromString("22f90b86-d083-4523-a253-e436a2a15146"),creatorID,true));//1
        testList.get(0).setPreviousMementoID(Constants.getNilUUID());
        testList.get(0).setNextMementoID(UUID.fromString("ff68458e-6900-4318-9b51-b1057444826e"));

        testList.add(new StateMemento(UUID.fromString("ff68458e-6900-4318-9b51-b1057444826e"),creatorID,true));//2
        testList.add(new StateMemento(UUID.fromString("d6914a99-aaa4-4c9e-a051-05dc985f0ecf"),creatorID,true));//3
        testList.add(new StateMemento(UUID.fromString("7ae4b8d4-fca0-4e49-92c9-8b23cb70ac21"),creatorID,true));//4
        testList.add(new StateMemento(UUID.fromString("9644c4bf-fd72-4ac6-83bd-091bb22991c4"),creatorID,true));//5
        testList.add(new StateMemento(UUID.fromString("3d872694-fd9a-4ad5-8ffb-64d81d78ed46"),creatorID,true));//6
        testList.add(new StateMemento(UUID.fromString("3d0e9bdd-a6a2-4ac1-82ba-823dc06c4316"),creatorID,true));//7
        testList.add(new StateMemento(UUID.fromString("8f04b5d5-efde-40f8-a251-4b5adbec19cb"),creatorID,true));//8
        testList.add(new StateMemento(UUID.fromString("9db45cb2-634c-4dcf-a09e-c7c35d3bee24"),creatorID,true));//9
        testList.add(new StateMemento(UUID.fromString("0a7b0367-e612-4690-99e8-f1c476ac2e82"),creatorID,true));//10

        for(int i=1; i<testList.size()-1; i++) {
            testList.get(i).setPreviousMementoID(testList.get(i-1).getId());
            testList.get(i).setNextMementoID(testList.get(i+1).getId());
        }

        testList.get(testList.size()-1).setPreviousMementoID(UUID.fromString("9db45cb2-634c-4dcf-a09e-c7c35d3bee24"));
        testList.get(testList.size()-1).setNextMementoID(Constants.getEndChainUUID());

        for(int i = 1; i<testList.size(); i++) {
            testList.get(i).setNextMementoID(Constants.getEndChainUUID());
            stateCaretaker.addMemento(testList.get(i),true);
        }
        ArrayList<StateMemento> subject = stateCaretaker.getMementos();
        for(int i = 0; i<subject.size(); i++) {
            assertEquals(testList.get(i).getId(), subject.get(i).getId());
        }
    }

    @Test
    public void localClearCommandTest() throws StateChainInconsistencyException {
        prepareStateCaretakerAndTestList();

        canvasController.processStateChangeCommand(testList.get(4).getId());
        assertEquals(testList.get(4).getId(),canvasController.getCurrentMementoID());

        ClearCommand clearCommand = canvasController.clearCanvas();

        StateMemento blankMemento = stateCaretaker.getMementoByIndex(5);
        assertEquals(blankMemento.getId(),clearCommand.getBlankMementoID());
        assertEquals(null,blankMemento.getPreviousMemento());
        assertEquals(Constants.getEndChainUUID(),blankMemento.getNextMemento().getId());
    }

    @Test
    public void localClearCommandAtEndTest() throws StateChainInconsistencyException {
        prepareStateCaretakerAndTestList();

        canvasController.processStateChangeCommand(testList.get(10).getId());
        assertEquals(testList.get(10).getId(),canvasController.getCurrentMementoID());

        ClearCommand clearCommand = canvasController.clearCanvas();

        StateMemento blankMemento = stateCaretaker.getMementoByIndex(11);
        assertEquals(blankMemento.getId(),clearCommand.getBlankMementoID());
        assertEquals(null,blankMemento.getPreviousMemento());
        assertEquals(Constants.getEndChainUUID(),blankMemento.getNextMementoID());
        assertEquals(blankMemento.getId(),stateCaretaker.getLastMementoID());
    }

    @Test
    public void remoteClearCommandTest() throws StateChainInconsistencyException {
        prepareStateCaretakerAndTestList();

        canvasController.processStateChangeCommand(testList.get(4).getId());
        assertEquals(testList.get(4).getId(),canvasController.getCurrentMementoID());

        ClearCommand clearCommand = new ClearCommand(canvasController,creatorID,
                UUID.fromString("11111111-b375-4899-85e2-50bd25ddba8f"),
                testList.get(4).getId(),
                testList.get(5).getId());
        clearCommand.setRemote(true);

        clearCommand.execute();
        StateMemento supposedBlankMemento = stateCaretaker.getMementoByIndex(5);
        assertEquals(clearCommand.getBlankMementoID(),supposedBlankMemento.getId());
        assertEquals(null,supposedBlankMemento.getPreviousMemento());
        assertEquals(testList.get(5).getId(),supposedBlankMemento.getNextMemento().getId());
    }

    @Test
    public void remoteClearCommandTestEnd() throws StateChainInconsistencyException {
        prepareStateCaretakerAndTestList();

        canvasController.processStateChangeCommand(testList.get(10).getId());
        assertEquals(testList.get(10).getId(),canvasController.getCurrentMementoID());

        ClearCommand clearCommand = new ClearCommand(canvasController,creatorID,
                UUID.fromString("11111111-b375-4899-85e2-50bd25ddba8f"),
                testList.get(10).getId(),
                Constants.getEndChainUUID());
        clearCommand.setRemote(true);

        clearCommand.execute();

        StateMemento blankMemento = stateCaretaker.getMementoByIndex(11);
        assertEquals(blankMemento.getId(),clearCommand.getBlankMementoID());
        assertEquals(null,blankMemento.getPreviousMemento());
        assertEquals(Constants.getEndChainUUID(),blankMemento.getNextMementoID());
        assertEquals(blankMemento.getId(),stateCaretaker.getLastMementoID());
        assertEquals(null,stateCaretaker.getMementoByID(blankMemento.getId()).getNextMemento());
    }
}
