package com.sharedtable.model.network.signals;

import com.sharedtable.model.network.NetworkClientEntity;
import com.sharedtable.model.network.NetworkClientEntityTree;

import java.util.ArrayList;
import java.util.UUID;

public class EntityTreeSignal implements Signal {

    private NetworkClientEntityTree entityTree;
    private UUID creatorID;

    public EntityTreeSignal(UUID creatorID,NetworkClientEntityTree entityTree) {
        this.creatorID = creatorID;
        this.entityTree = entityTree;
    }

    public EntityTreeSignal(String[] input) {
        entityTree = new NetworkClientEntityTree();
        creatorID = UUID.fromString(input[2]);
        ArrayList<NetworkClientEntity> res = new ArrayList<>();
        for(int i=3; i<input.length; i=i+9){//MODIFY i=i+9!
            String[] partRes = new String[9]; //MODIFY 9!
            partRes[0] = input[i];
            partRes[1] = input[i+1];
            partRes[2] = input[i+2];
            partRes[3] = input[i+3];
            partRes[4] = input[i+4];
            partRes[5] = input[i+5];
            partRes[6] = input[i+6];
            partRes[7] = input[i+7];
            partRes[8] = input[i+8];
            NetworkClientEntity entity = new NetworkClientEntity(partRes);
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



}
