package com.sharedtable.model.signals;

import com.sharedtable.model.ArrayPrinter;
import com.sharedtable.model.NetworkClientEntity;
import com.sharedtable.model.NetworkClientEntityTree;

import java.util.ArrayList;
import java.util.UUID;

public class EntityTreeSignal implements Signal {

    public EntityTreeSignal(UUID creatorID,NetworkClientEntityTree entityTree) {
        this.creatorID = creatorID;
        this.entityTree = entityTree;
    }

    public EntityTreeSignal(String[] input) {
        entityTree = new NetworkClientEntityTree();
        creatorID = UUID.fromString(input[2]);
        ArrayList<NetworkClientEntity> res = new ArrayList<>();
        for(int i=3; i<input.length; i=i+6) {
            String[] pres = new String[6];
            pres[0] = input[i];
            pres[1] = input[i+1];
            pres[2] = input[i+2];
            pres[3] = input[i+3];
            pres[4] = input[i+4];
            pres[5] = input[i+5];
            NetworkClientEntity entity = new NetworkClientEntity(pres);
            res.add(entity);
        }
        entityTree.addNetworkClientEntities(res);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;TREE;").append(creatorID.toString()).append(";");
        for(NetworkClientEntity act : entityTree.getAllClients()) {
            sb.append(act.toString());
        }
        sb.append(";");
        return sb.toString();
    }

    public NetworkClientEntityTree getEntityTree() {
        return entityTree;
    }

    public UUID getCreatorID() {
        return creatorID;
    }


    private NetworkClientEntityTree entityTree;
    private UUID creatorID;
}
