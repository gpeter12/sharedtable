package com.sharedtable.model;

import com.sharedtable.controller.UserID;

import java.util.ArrayList;
import java.util.UUID;

public class NetworkClientEntityTree {
    public NetworkClientEntityTree(NetworkClientEntity me) {
        root = me;
    }

    public void setRoot(NetworkClientEntity newRoot) {
        /*if(newRoot.getUpperClientEntity() != null)
            throw new RuntimeException("Bad root given to set root!");
        if(!(root.getUpperClientEntity().getID().equals(newRoot.getID()))) {
            removeNetworkClientEntityWithException(root, newRoot);
        }
        else
            root.setUpperClientEntity(newRoot);*/
        root = newRoot;
    }

    public void setNewConnection(UUID child, UUID parent) {
        getNetworkClientEntity(child).setUpperClientEntity(getNetworkClientEntity(parent));
    }

    public NetworkClientEntity getRoot() {
        return root;
    }

    public void addNetworkClientEntity(NetworkClientEntity entity) {
        /*if(!UserID.getUserID().equals(entity.getID()) &&
                !clients.contains(entity))
        {
            if(entity.getUpperClientID() == null)
                setRoot(entity);
            else
                entity.setUpperClientEntity(getNetworkClientEntity(entity.getUpperClientID()));*/
            clients.add(entity);
        //}
    }

    public void removeAllChildren(NetworkClientEntity entity) {
        for(NetworkClientEntity act : getAllChildren(entity)) {
            clients.remove(act);
        }
    }

    public void removeCloseChildren(NetworkClientEntity entity) {
        for(NetworkClientEntity act : getCloseChildren(entity)) {
            clients.remove(act);
        }
    }

    public void removeNetworkClientEntity(NetworkClientEntity entity) {
        removeAllChildren(entity);
        clients.remove(entity);
    }

    public void removeNetworkClientEntityWithException(NetworkClientEntity entity,NetworkClientEntity exception) {
        if(entity.getID().equals(exception.getID()))
            return;
        for(NetworkClientEntity act : getCloseChildren(entity)) {
            if(!act.equals(exception)){
                removeNetworkClientEntityWithException(act,exception);
            }
        }
        removeCloseChildren(entity);
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

    public boolean contains(UUID id) {
        for(NetworkClientEntity act : clients) {
            if(act.getID().equals(id)){
                return true;
            }
        }
        return false;
    }

    public ArrayList<NetworkClientEntity> getAllClients() {
        ArrayList<NetworkClientEntity> res = new ArrayList<>();
        res.addAll(getAllChildren(root));
        return res;
    }

    private ArrayList<NetworkClientEntity> clients = new ArrayList<>();
    private NetworkClientEntity root = null;
}
