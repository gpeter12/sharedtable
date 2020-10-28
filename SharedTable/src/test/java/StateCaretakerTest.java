import com.sharedtable.Constants;
import com.sharedtable.Utils;
import com.sharedtable.controller.StateCaretaker;
import com.sharedtable.controller.StateChainInconsistencyException;
import com.sharedtable.controller.StateMemento;
import com.sharedtable.controller.StateOriginator;
import com.sharedtable.model.network.NetworkClientEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class StateCaretakerTest {

    private StateCaretaker stateCaretaker = new StateCaretaker();
    private StateOriginator stateOriginator = new StateOriginator();
    private ArrayList<StateMemento> testList = new ArrayList<>();
    private UUID creatorID = UUID.fromString("1c183421-b375-4899-85e2-50bd25ddba8f");

    public StateCaretakerTest() {
        prepareStateCaretaker();
    }

    private StateMemento makeFirstMemento() {
        StateMemento firstMemento = stateOriginator.createMemento();
        firstMemento.setId(Constants.getNilUUID());
        firstMemento.setPreviousMementoID(Constants.getNilUUID());
        firstMemento.setNextMementoID(Constants.getEndChainUUID());
        return firstMemento;
    }
    
    private boolean areTheyNeighboursAccessor(UUID a, UUID b) {
        Method method = null;
        try {
            method = stateCaretaker.getClass().getDeclaredMethod("areTheyNeighbours",UUID.class,UUID.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        method.setAccessible(true);
        Object r = null;
        try {
            r = method.invoke(stateCaretaker,a,b);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return (boolean)r;
    }

    private StateMemento getColliderMementoAccessor(StateMemento memento) {
        Method method = null;
        try {
            method = stateCaretaker.getClass().getDeclaredMethod("getColliderMemento",StateMemento.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        method.setAccessible(true);
        Object r = null;
        try {
            r = method.invoke(stateCaretaker,memento);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return (StateMemento) r;
    }

    private void prepareStateCaretaker() {
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
    }

    @Test
    public void simpleAddMementoTest() throws StateChainInconsistencyException {
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
    public void mementoInsertionTest() throws StateChainInconsistencyException {
        stateCaretaker.clearStateChain();
        stateCaretaker.addFirstMemento(makeFirstMemento());
        simpleAddMementoTest();

        StateMemento mementoToInsert = new StateMemento(UUID.fromString("11111111-b375-4899-85e2-50bd25ddba8f"),creatorID,true);
        mementoToInsert.setPreviousMementoID(UUID.fromString("8f04b5d5-efde-40f8-a251-4b5adbec19cb"));
        mementoToInsert.setNextMementoID(UUID.fromString("9db45cb2-634c-4dcf-a09e-c7c35d3bee24"));
        stateCaretaker.addMemento(mementoToInsert,true);

        assertEquals(testList.get(7).getId(),stateCaretaker.getMementoByIndex(7).getId());
        assertEquals(testList.get(8).getId(),stateCaretaker.getMementoByIndex(8).getId());
        assertEquals(mementoToInsert.getId(),stateCaretaker.getMementoByIndex(9).getId());
        assertEquals(testList.get(9).getId(),stateCaretaker.getMementoByIndex(10).getId());

        assertEquals(testList.get(8).getId(),stateCaretaker.getMementoByIndex(9).getPreviousMementoID());
        assertEquals(testList.get(9).getId(),stateCaretaker.getMementoByIndex(9).getNextMementoID());
        assertEquals(testList.get(8).getId(),stateCaretaker.getMementoByIndex(9).getPreviousMemento().getId());
        assertEquals(testList.get(9).getId(),stateCaretaker.getMementoByIndex(9).getNextMemento().getId());


    }

    @Test
    public void areTheyNeighboursTest() throws StateChainInconsistencyException {
        stateCaretaker.clearStateChain();
        stateCaretaker.addFirstMemento(makeFirstMemento());
        simpleAddMementoTest();

        assertTrue(areTheyNeighboursAccessor(stateCaretaker.getMementoByIndex(3).getId(),
                stateCaretaker.getMementoByIndex(4).getId()));
        assertTrue(areTheyNeighboursAccessor(stateCaretaker.getMementoByIndex(8).getId(),
                stateCaretaker.getMementoByIndex(9).getId()));
        assertFalse(areTheyNeighboursAccessor(stateCaretaker.getMementoByIndex(3).getId(),
                stateCaretaker.getMementoByIndex(5).getId()));

        assertTrue(areTheyNeighboursAccessor(stateCaretaker.getMementoByIndex(4).getId(),
                stateCaretaker.getMementoByIndex(3).getId()));
        assertFalse(areTheyNeighboursAccessor(stateCaretaker.getMementoByIndex(5).getId(),
                stateCaretaker.getMementoByIndex(3).getId()));

    }

    @Test
    public void mementoInsertionWithCollisionTestA() throws StateChainInconsistencyException {
        stateCaretaker.clearStateChain();
        stateCaretaker.addFirstMemento(makeFirstMemento());
        simpleAddMementoTest();

        StateMemento mementoToInsert1 = new StateMemento(UUID.fromString("11111111-b375-4899-85e2-50bd25ddba8f"),creatorID,true);
        mementoToInsert1.setPreviousMementoID(UUID.fromString("8f04b5d5-efde-40f8-a251-4b5adbec19cb"));
        mementoToInsert1.setNextMementoID(UUID.fromString("9db45cb2-634c-4dcf-a09e-c7c35d3bee24"));
        stateCaretaker.addMemento(mementoToInsert1,true);

        StateMemento mementoToInsert2 = new StateMemento(UUID.fromString("22222222-b375-4899-85e2-50bd25ddba8f"),creatorID,true);
        mementoToInsert2.setPreviousMementoID(UUID.fromString("8f04b5d5-efde-40f8-a251-4b5adbec19cb"));
        mementoToInsert2.setNextMementoID(UUID.fromString("9db45cb2-634c-4dcf-a09e-c7c35d3bee24"));

        assertNotNull(getColliderMementoAccessor(mementoToInsert2));

        stateCaretaker.addMemento(mementoToInsert2,true);

        assertEquals(testList.get(7).getId(),stateCaretaker.getMementoByIndex(7).getId());
        assertEquals(testList.get(8).getId(),stateCaretaker.getMementoByIndex(8).getId());
        assertEquals(mementoToInsert1.getId(),stateCaretaker.getMementoByIndex(9).getId());
        assertEquals(mementoToInsert2.getId(),stateCaretaker.getMementoByIndex(10).getId());
        assertEquals(testList.get(9).getId(),stateCaretaker.getMementoByIndex(11).getId());
    }

    @Test
    public void mementoInsertionWithCollisionTestB() throws StateChainInconsistencyException {
        stateCaretaker.clearStateChain();
        stateCaretaker.addFirstMemento(makeFirstMemento());
        simpleAddMementoTest();

        StateMemento mementoToInsert1 = new StateMemento(UUID.fromString("22222222-b375-4899-85e2-50bd25ddba8f"),creatorID,true);
        mementoToInsert1.setPreviousMementoID(UUID.fromString("8f04b5d5-efde-40f8-a251-4b5adbec19cb"));
        mementoToInsert1.setNextMementoID(UUID.fromString("9db45cb2-634c-4dcf-a09e-c7c35d3bee24"));
        stateCaretaker.addMemento(mementoToInsert1,true);

        StateMemento mementoToInsert2 = new StateMemento(UUID.fromString("11111111-b375-4899-85e2-50bd25ddba8f"),creatorID,true);
        mementoToInsert2.setPreviousMementoID(UUID.fromString("8f04b5d5-efde-40f8-a251-4b5adbec19cb"));
        mementoToInsert2.setNextMementoID(UUID.fromString("9db45cb2-634c-4dcf-a09e-c7c35d3bee24"));
        stateCaretaker.addMemento(mementoToInsert2,true);

        assertEquals(testList.get(7).getId(),stateCaretaker.getMementoByIndex(7).getId());
        assertEquals(testList.get(8).getId(),stateCaretaker.getMementoByIndex(8).getId());
        assertEquals(mementoToInsert2.getId(),stateCaretaker.getMementoByIndex(9).getId());
        assertEquals(mementoToInsert1.getId(),stateCaretaker.getMementoByIndex(10).getId());
        assertEquals(testList.get(9).getId(),stateCaretaker.getMementoByIndex(11).getId());

        assertEquals(testList.get(8).getId(),stateCaretaker.getMementoByID(mementoToInsert2.getId()).getPreviousMementoID());
        assertEquals(mementoToInsert1.getId(),stateCaretaker.getMementoByID(mementoToInsert2.getId()).getNextMementoID());

        assertEquals(mementoToInsert2.getId(),stateCaretaker.getMementoByID(mementoToInsert1.getId()).getPreviousMementoID());
        assertEquals(testList.get(9).getId(),stateCaretaker.getMementoByID(mementoToInsert1.getId()).getNextMementoID());

        assertEquals(testList.get(8).getId(),stateCaretaker.getMementoByID(mementoToInsert2.getId()).getPreviousMemento().getId());
        assertEquals(mementoToInsert1.getId(),stateCaretaker.getMementoByID(mementoToInsert2.getId()).getNextMemento().getId());

        assertEquals(mementoToInsert2.getId(),stateCaretaker.getMementoByID(mementoToInsert1.getId()).getPreviousMemento().getId());
        assertEquals(testList.get(9).getId(),stateCaretaker.getMementoByID(mementoToInsert1.getId()).getNextMemento().getId());
    }

    @Test
    public void mementoInsertionWithCollisionTestC() throws StateChainInconsistencyException {
        stateCaretaker.clearStateChain();
        stateCaretaker.addFirstMemento(makeFirstMemento());
        simpleAddMementoTest();

        StateMemento mementoToInsert1 = new StateMemento(UUID.fromString("11111111-b375-4899-85e2-50bd25ddba8f"),creatorID,true);
        mementoToInsert1.setPreviousMementoID(UUID.fromString("0a7b0367-e612-4690-99e8-f1c476ac2e82"));
        mementoToInsert1.setNextMementoID(Constants.getEndChainUUID());
        stateCaretaker.addMemento(mementoToInsert1,true);

        StateMemento mementoToInsert2 = new StateMemento(UUID.fromString("22222222-b375-4899-85e2-50bd25ddba8f"),creatorID,true);
        mementoToInsert2.setPreviousMementoID(UUID.fromString("0a7b0367-e612-4690-99e8-f1c476ac2e82"));
        mementoToInsert2.setNextMementoID(Constants.getEndChainUUID());
        stateCaretaker.addMemento(mementoToInsert2,true);


        assertEquals(mementoToInsert1.getId(),stateCaretaker.getMementoByIndex(11).getId());
        assertEquals(mementoToInsert2.getId(),stateCaretaker.getMementoByIndex(12).getId());

    }

    @Test
    public void mementoInsertionWithCollisionTestD() throws StateChainInconsistencyException {
        stateCaretaker.clearStateChain();
        stateCaretaker.addFirstMemento(makeFirstMemento());
        simpleAddMementoTest();

        StateMemento mementoToInsert1 = new StateMemento(UUID.fromString("11111111-b375-4899-85e2-50bd25ddba8f"),creatorID,true);
        mementoToInsert1.setPreviousMementoID(Constants.getNilUUID());
        mementoToInsert1.setNextMementoID(UUID.fromString("22f90b86-d083-4523-a253-e436a2a15146"));
        stateCaretaker.addMemento(mementoToInsert1,true);

        StateMemento mementoToInsert2 = new StateMemento(UUID.fromString("22222222-b375-4899-85e2-50bd25ddba8f"),creatorID,true);
        mementoToInsert2.setPreviousMementoID(Constants.getNilUUID());
        mementoToInsert2.setNextMementoID(UUID.fromString("22f90b86-d083-4523-a253-e436a2a15146"));
        stateCaretaker.addMemento(mementoToInsert2,true);

        assertEquals(mementoToInsert1.getId(),stateCaretaker.getMementoByIndex(1).getId());
        assertEquals(mementoToInsert2.getId(),stateCaretaker.getMementoByIndex(2).getId());

    }

    @Test
    public void mementoAddWClenupTest() throws StateChainInconsistencyException {
        stateCaretaker.clearStateChain();
        stateCaretaker.addFirstMemento(makeFirstMemento());
        simpleAddMementoTest();

        StateMemento mementoToInsert1 = new StateMemento(UUID.fromString("11111111-b375-4899-85e2-50bd25ddba8f"),creatorID,true);
        mementoToInsert1.setPreviousMementoID(UUID.fromString("7ae4b8d4-fca0-4e49-92c9-8b23cb70ac21"));
        mementoToInsert1.setNextMementoID(UUID.fromString("9644c4bf-fd72-4ac6-83bd-091bb22991c4"));
        stateCaretaker.addMementoWCleanup(mementoToInsert1,mementoToInsert1.getPreviousMementoID(),
                mementoToInsert1.getNextMementoID(),true);

        assertEquals(mementoToInsert1.getId(),stateCaretaker.getMementoByIndex(5).getId());
        assertEquals(Constants.getEndChainUUID(),stateCaretaker.getMementoByIndex(5).getNextMementoID());

    }
}
