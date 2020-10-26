package com.sharedtable.model.network;

import com.sharedtable.controller.UserID;

import java.util.ArrayList;
import java.util.UUID;

public class NetworkClientEntityTree {

    private ArrayList<NetworkClientEntity> clients = new ArrayList<>();


    public NetworkClientEntityTree(NetworkClientEntity me) {
        clients.add(me);
        setMeRoot();
    }

    public NetworkClientEntityTree() {
    }

    public void setMeRoot() {
        getNetworkClientEntity(UserID.getInstance().getUserID()).setUpperClientEntity(null);
    }

    public NetworkClientEntity getRoot() {
        return findRoot();
    }

    public void addNetworkClientEntities(ArrayList<NetworkClientEntity> entities) {
        clients.addAll(entities);
    }

    public void addNetworkClientEntity(NetworkClientEntity entity) {
        if(!UserID.getInstance().getUserID().equals(entity.getID()) &&
                !clients.contains(entity))
        {
            System.out.println("ADDING entity: "+entity.getID());
            clients.add(entity);
        } else {
            System.out.println("client already in the NetworkClientEntityTree!: "+entity.getID());
        }
    }

    public NetworkClientEntity getNetworkClientEntity(UUID id) {
        for(NetworkClientEntity act : clients) {
            if(act.getID().equals(id))
                return act;
        }
        throw new RuntimeException("Client not found by ID: "+id.toString());
    }

    public ArrayList<NetworkClientEntity> getCloseChildren(NetworkClientEntity entity) {
        ArrayList<NetworkClientEntity> res = new ArrayList<>();
        for(NetworkClientEntity act : clients) {
            if(act.getUpperClientID() == null)//a root senkinek a gyereke
                continue;
            if(act.getUpperClientID().equals(entity.getID()))
                res.add(act);
        }
        return res;
    }

    public boolean contains(UUID id) {
        for(NetworkClientEntity act : clients) {
            if(act.getID().equals(id)){
                return true;
            }
        }
        return false;
    }

    public NetworkClientEntity findRoot() {
        for(NetworkClientEntity act : clients) {
            if(act.getUpperClientID() == null) {
                return act;
            }
        }
        throw new RuntimeException("There is no Root in NetworkClientEntityTree");
    }

    public ArrayList<NetworkClientEntity> getAllClients() {
        ArrayList<NetworkClientEntity> res = new ArrayList<>();
        for(NetworkClientEntity act : clients) {
            res.add(act);
        }
        return res;
    }

    public void clearAll() {clients.clear();}

}
