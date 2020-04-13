package com.sharedtable.controller;

import com.sharedtable.model.Network.NetworkClientEntity;
import com.sharedtable.model.Network.NetworkClientEntityTree;
import com.sharedtable.model.Network.NetworkService;
import com.sharedtable.view.ClientPropertyWindowView;
import com.sharedtable.view.ClientsWindowView;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;

public class ClientsWindowController implements Initializable, NotifyableClientEntityTreeChange {

    public ClientsWindowController() {
    }


    private void setMouseDoubleClickEvent() {
        treeView.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent mouseEvent)
            {
                if(mouseEvent.getClickCount() == 2)
                {
                    onMouseDoubleClick();
                }
            }
        });
    }

    private void onMouseDoubleClick() {
        UUID clientID;
        try{
        TreeItem item = (TreeItem)treeView.getSelectionModel().getSelectedItem();
        clientID = UUID.fromString(((Text)item.getValue()).getId());}
        catch (Exception e) {return;}
        if(clientsWindowView == null) {
            throw new RuntimeException("clientsWindowView was not set previously!");
        }
        new ClientPropertyWindowView(NetworkService.getEntityTree().getNetworkClientEntity(clientID),clientsWindowView);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        init();
    }

    private synchronized void init() {
        NetworkClientEntity rootEntity = NetworkService.getEntityTree().getRoot();

        TreeItem rootChild = createTreeItem(rootEntity.getNickname(),
                UserID.getUserID().equals(rootEntity.getID()),
                UserID.getUserID().equals(rootEntity.getID()),
                rootEntity.getID().toString());


        rootItem.getChildren().add(rootChild);
        loadChildData(rootChild, NetworkService.getEntityTree(),NetworkService.getEntityTree().getRoot());

        setMouseDoubleClickEvent();
        NetworkService.subscribeForClientEntityTreeChange(this);
    }


    private TreeItem<Text> createTreeItem(String text, boolean isUnderLine, boolean isBold, String ItemId) {
        Text resText = new Text(text);
        resText.setUnderline(isUnderLine);
        resText.setId(ItemId);
        if(isBold)
            resText.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        else
            resText.setFont(Font.font("Verdana",  13));
        TreeItem<Text> resTI = new TreeItem<Text>(resText);
        resTI.setExpanded(true);
        return resTI;
    }


    private void loadChildData(TreeItem viewRoot, NetworkClientEntityTree entityTree, NetworkClientEntity entityTreeRoot) {
        var children = entityTree.getCloseChildren(entityTreeRoot);
        if(children.isEmpty())
            return;
        for(NetworkClientEntity act : children) {

            TreeItem<Text> child = createTreeItem(act.getNickname(),UserID.getUserID().equals(act.getID()),
                    UserID.getUserID().equals(act.getID()),act.getID().toString());
            viewRoot.getChildren().add(child);
            loadChildData(child,entityTree,act);
        }
    }

    @Override
    public void notifyClientEntityTreeChange() {
        Platform.runLater(() -> {
            rootItem = createTreeItem("Minden kliens a hálózatban",false,false,"root");
            rootItem.setExpanded(true);
            treeView.setRoot(rootItem);
            init();
        });
    }

    public void onClose() {
        NetworkService.unSubscribeForClientEntityTreeChange(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientsWindowController that = (ClientsWindowController) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void setView(ClientsWindowView clientsWindowView) {
        this.clientsWindowView = clientsWindowView;
    }

    @FXML
    private TreeItem<Text> rootItem;
    @FXML
    private TreeView treeView;
    private UUID id = UUID.randomUUID();
    private ClientsWindowView clientsWindowView;

}
