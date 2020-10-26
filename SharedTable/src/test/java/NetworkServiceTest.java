import com.sharedtable.Constants;
import com.sharedtable.controller.UserID;
import com.sharedtable.model.network.NetworkClientEntity;
import com.sharedtable.model.network.NetworkClientEntityTree;
import com.sharedtable.model.network.NetworkService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

public class NetworkServiceTest {

    private NetworkService networkService;
    private NetworkClientEntityTree entityTree;
    private UserID userID = UserID.getInstance();
    private NetworkClientEntity me;

    public NetworkServiceTest() {
        userID.initWithoutPersistence(UUID.fromString("1c183421-b375-4899-85e2-50bd25ddba8f"),
                "testUser","0.0.0.0");
        networkService = NetworkService.getInstance();
        networkService.initService();
        entityTree = networkService.getEntityTree();
        networkService.switchToTestMode();
        me = entityTree.getNetworkClientEntity(UUID.fromString("1c183421-b375-4899-85e2-50bd25ddba8f"));
    }


    /*
        root (disconnect)
        └── thisClient (becomes new root)
     */
    @Test
    public void rootDisconnectWithNoSiblings() {
        entityTree.addNetworkClientEntity(new NetworkClientEntity(Constants.getNilUUID(),"theRoot","0.0.0.0",
                30000,1,null,27));
        assertNull(networkService.findNewUpperClientEntityToConnect(Constants.getNilUUID()));
        assertNull(networkService.findNewSiblingClientEntityToConnect(Constants.getNilUUID()));
        entityTree.clearAll();
    }


    /*
        root
        ├── sibling1
        ├── sibling2
        ├── sibling3
        └── thisClient
     */
    @Test
    public void rootDisconnectWSiblings() {
        NetworkClientEntity root = new NetworkClientEntity(Constants.getNilUUID(),"theRoot","0.0.0.0",
                30000,1,null,27);
        entityTree.addNetworkClientEntity(root);
        NetworkClientEntity sibling1 = new NetworkClientEntity(UUID.fromString("11111111-b375-4899-85e2-50bd25ddba8f"),"sibling","0.0.0.0",
                30000,1,null,27);
        sibling1.setUpperClientEntity(root);
        sibling1.setPort(25000);
        entityTree.getNetworkClientEntity(UUID.fromString("1c183421-b375-4899-85e2-50bd25ddba8f")).setUpperClientEntity(root);
        entityTree.addNetworkClientEntity(sibling1);

        assertNull(networkService.findNewUpperClientEntityToConnect(root.getID()));
        assertEquals(sibling1.getID(),networkService.findNewSiblingClientEntityToConnect(root.getID()).getID());

        //with enabled incoming connections
        entityTree.getNetworkClientEntity(UUID.fromString("1c183421-b375-4899-85e2-50bd25ddba8f")).setPort(26000);
        assertNull(networkService.findNewUpperClientEntityToConnect(root.getID()));
        assertEquals(sibling1.getID(),networkService.findNewSiblingClientEntityToConnect(root.getID()).getID());

        //with enabled incoming connections and 3 siblings
        NetworkClientEntity sibling2 = new NetworkClientEntity(UUID.fromString("22222222-b375-4899-85e2-50bd25ddba8f"),"sibling2","0.0.0.0",
                30000,1,null,27);
        sibling2.setUpperClientEntity(root);
        sibling2.setPort(25000);
        entityTree.addNetworkClientEntity(sibling2);

        NetworkClientEntity sibling3 = new NetworkClientEntity(UUID.fromString("33333333-b375-4899-85e2-50bd25ddba8f"),"sibling3","0.0.0.0",
                30000,1,null,27);
        sibling3.setUpperClientEntity(root);
        sibling3.setPort(25000);
        entityTree.addNetworkClientEntity(sibling3);

        assertNull(networkService.findNewUpperClientEntityToConnect(root.getID()));
        assertEquals(sibling3.getID(),networkService.findNewSiblingClientEntityToConnect(root.getID()).getID());

        //with sibling3 inactive
        entityTree.getNetworkClientEntity(sibling3.getID()).setPort(-1);
        assertNull(networkService.findNewUpperClientEntityToConnect(root.getID()));
        assertEquals(sibling2.getID(),networkService.findNewSiblingClientEntityToConnect(root.getID()).getID());

        entityTree.clearAll();
    }

    /*
        root
        ├── client1
        ├── client2
        │    └── thisClient
        └── client3

     */
    @Test
    public void sibling2DisconnectTest() {
        NetworkClientEntity root = new NetworkClientEntity(Constants.getNilUUID(),"theRoot","0.0.0.0",
                30000,1,null,27);
        entityTree.addNetworkClientEntity(root);

        NetworkClientEntity client1 = new NetworkClientEntity(UUID.fromString("11111111-b375-4899-85e2-50bd25ddba8f"),"client1","0.0.0.0",
                30000,1,null,27);
        client1.setUpperClientEntity(root);
        client1.setPort(25000);
        entityTree.addNetworkClientEntity(client1);

        NetworkClientEntity client2 = new NetworkClientEntity(UUID.fromString("22222222-b375-4899-85e2-50bd25ddba8f"),"client2","0.0.0.0",
                30000,1,null,27);
        client2.setUpperClientEntity(root);
        client2.setPort(25000);
        entityTree.addNetworkClientEntity(client2);

        NetworkClientEntity client3 = new NetworkClientEntity(UUID.fromString("33333333-b375-4899-85e2-50bd25ddba8f"),"client3","0.0.0.0",
                30000,1,null,27);
        client3.setUpperClientEntity(root);
        client3.setPort(25000);
        entityTree.addNetworkClientEntity(client3);

        entityTree.getNetworkClientEntity(UUID.fromString("1c183421-b375-4899-85e2-50bd25ddba8f")).setUpperClientEntity(client2);

        assertEquals(client1.getID(),networkService.findNewUpperClientEntityToConnect(client2.getID()).getID());

        //sibling 1,3 unable to receive connections

        client1.setPort(-1);
        client3.setPort(-1);

        assertEquals(root.getID(),networkService.findNewUpperClientEntityToConnect(client2.getID()).getID());

    }

    /*
    root
    ├── client1
    └── client2
        ├── client3
        └── client4
            └── thisClient
     */
    @Test
    public void Layer3Test() {
        entityTree.clearAll();

        NetworkClientEntity root = new NetworkClientEntity(Constants.getNilUUID(),"theRoot","0.0.0.0",
                30000,1,null,27);
        entityTree.addNetworkClientEntity(root);

        NetworkClientEntity client1 = new NetworkClientEntity(UUID.fromString("11111111-b375-4899-85e2-50bd25ddba8f"),"client1","0.0.0.0",
                30000,1,null,27);
        client1.setUpperClientEntity(root);
        client1.setPort(25000);
        entityTree.addNetworkClientEntity(client1);

        NetworkClientEntity client2 = new NetworkClientEntity(UUID.fromString("22222222-b375-4899-85e2-50bd25ddba8f"),"client2","0.0.0.0",
                30000,1,null,27);
        client2.setUpperClientEntity(root);
        client2.setPort(25000);
        entityTree.addNetworkClientEntity(client2);

        NetworkClientEntity client3 = new NetworkClientEntity(UUID.fromString("33333333-b375-4899-85e2-50bd25ddba8f"),"client3","0.0.0.0",
                30000,1,null,27);
        client3.setUpperClientEntity(client2);
        client3.setPort(-1);
        entityTree.addNetworkClientEntity(client3);

        NetworkClientEntity client4 = new NetworkClientEntity(UUID.fromString("44444444-b375-4899-85e2-50bd25ddba8f"),"client4","0.0.0.0",
                30000,1,null,27);
        client4.setUpperClientEntity(client2);
        client4.setPort(25000);
        entityTree.addNetworkClientEntity(client4);

        entityTree.addNetworkClientEntity(me);
        me.setUpperClientEntity(client4);

        //client4 disconnects and client3 inactive
        assertEquals(client2.getID(),networkService.findNewUpperClientEntityToConnect(client4.getID()).getID());

        //client1 inactive
        client1.setPort(-1);
        assertEquals(client2.getID(),networkService.findNewUpperClientEntityToConnect(client4.getID()).getID());

        //client3 active
        client3.setPort(25000);
        assertEquals(client3.getID(),networkService.findNewUpperClientEntityToConnect(client4.getID()).getID());
    }

}
