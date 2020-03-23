package com.sharedtable.model;

import java.util.ArrayList;
import java.util.UUID;

public class NetworkClientEntityTree {
    public NetworkClientEntityTree(NetworkClientEntity me) {
        root = me;
    }

    public void setRoot(NetworkClientEntity entity) {
        root = entity;
    }

    public NetworkClientEntity getRoot() {
        return root;
    }

    public void addNetworkClientEntity(NetworkClientEntity entity) {
        entity.setUpperClientEntity(getNetworkClientEntity(entity.getUpperClientID()));
    }

    public void removeAllChildren(NetworkClientEntity entity) {
        for(NetworkClientEntity act : getAllChildren(entity)) {
            clients.remove(act);
        }
    }

    public void removeNetworkClientEntity(NetworkClientEntity entity) {
        removeAllChildren(entity);
        clients.remove(entity);
    }

    public void removeNetworkClientEntity(UUID id) {
        removeNetworkClientEntity(getNetworkClientEntity(id));
    }

    public NetworkClientEntity getNetworkClientEntity(UUID id) {
        for(NetworkClientEntity act : clients) {
            if(act.getID().equals(id))
                return act;
        }
        throw new RuntimeException("Client not found by ID");
    }

    public ArrayList<NetworkClientEntity> getCloseChildren(NetworkClientEntity entity) {
        ArrayList<NetworkClientEntity> res = new ArrayList<>();
        for(NetworkClientEntity act : clients) {
            if(act.getUpperClientEntity().getID().equals(entity.getID()))
                res.add(act);
        }
        return res;
    }

    public ArrayList<NetworkClientEntity> getAllChildren(NetworkClientEntity entity) {
        ArrayList<NetworkClientEntity> res = new ArrayList<>();
        for(NetworkClientEntity act : clients) {
            if(act.getUpperClientEntity().getID().equals(entity.getID())){
                res.addAll(getAllChildren(entity));
            }
        }
        res.add(entity);
        res.addAll(getCloseChildren(entity));
        return res;
    }

    public NetworkClientEntity getParent(UUID uuid) {
        return getNetworkClientEntity(uuid).getUpperClientEntity();
    }

    private ArrayList<NetworkClientEntity> clients = new ArrayList<>();
    private NetworkClientEntity root = null;
}
