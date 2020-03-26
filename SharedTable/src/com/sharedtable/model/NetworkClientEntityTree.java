package com.sharedtable.model;

import com.sharedtable.controller.UserID;

import java.util.ArrayList;
import java.util.UUID;

public class NetworkClientEntityTree {
    public NetworkClientEntityTree(NetworkClientEntity me) {
        clients.add(me);
        setMeRoot();
    }

    public NetworkClientEntityTree() {
    }

    public void setMeRoot() {
        getNetworkClientEntity(UserID.getUserID()).setUpperClientEntity(null);
    }

    public NetworkClientEntity getRoot() {
        return findRoot();
    }

    public void addNetworkClientEntities(ArrayList<NetworkClientEntity> entities) {
        clients.addAll(entities);
    }

    public void addNetworkClientEntity(NetworkClientEntity entity) {
        if(!UserID.getUserID().equals(entity.getID()) &&
                !clients.contains(entity))
        {
            //if(entity.getUpperClientID()!=null)
                //entity.setUpperClientEntity(getNetworkClientEntity(entity.getUpperClientID()));
            System.out.println("ADDING entity: "+entity.getID());
            clients.add(entity);
        } else {
            System.out.println("client already in the NetworkClientEntityTree!: "+entity.getID());
        }
    }

    /*public void removeAllChildren(NetworkClientEntity entity) {
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
    }*/

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

    public ArrayList<NetworkClientEntity> getAllChildren(NetworkClientEntity entity) {
        ArrayList<NetworkClientEntity> res = new ArrayList<>();
        for(NetworkClientEntity act : getCloseChildren(entity)) {
            res.add(act);
            res.addAll(getAllChildren(act));
        }
        return res;
    }

    /*public NetworkClientEntity getParent(UUID uuid) {
        return getNetworkClientEntity(uuid).getUpperClientEntity();
    }*/

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
        /*ArrayList<NetworkClientEntity> res = new ArrayList<>();
        res.addAll(getAllChildren(findRoot()));
        res.add(findRoot());*/
        return clients;
    }

    /*public boolean isInPathBetween(NetworkClientEntity entityFrom, NetworkClientEntity entityTo,
                                   NetworkClientEntity entity)
    {
        NetworkClientEntity act = entityFrom;
        while (!act.equals(entityTo)){
            if(act.equals(entity)) {
                return true;
            }
            act = act.getUpperClientEntity();
        }
        return false;
    }

    public NetworkClientEntity getChildUnderEntityOnPathBetween(NetworkClientEntity entityFrom, NetworkClientEntity entityTo,
                                             NetworkClientEntity entity) {
        NetworkClientEntity act = entityFrom;
        NetworkClientEntity prev= null;
        while (!act.equals(entityTo)){
            if(act.equals(entity)) {
                return prev;
            }
            prev = act;
            act = act.getUpperClientEntity();
        }
        throw new RuntimeException("entity not found on path");
    }*/

    private ArrayList<NetworkClientEntity> clients = new ArrayList<>();

}
