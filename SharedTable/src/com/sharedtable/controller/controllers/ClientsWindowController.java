package com.sharedtable.controller.controllers;

import com.sharedtable.controller.UserID;
import com.sharedtable.model.NetworkClientEntity;
import com.sharedtable.model.NetworkClientEntityTree;
import com.sharedtable.model.NetworkService;
import com.sharedtable.view.ClientPropertyWindowView;
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
import java.util.ResourceBundle;
import java.util.UUID;

public class ClientsWindowController implements Initializable {

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
        new ClientPropertyWindowView(NetworkService.getEntityTree().getNetworkClientEntity(clientID));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        NetworkClientEntity rootEntity = NetworkService.getEntityTree().getRoot();

        TreeItem rootChild = createTreeItem(rootEntity.getID().toString(),
                UserID.getUserID().equals(rootEntity.getID()),
                UserID.getUserID().equals(rootEntity.getID()),
                        rootEntity.getID().toString());


        rootItem.getChildren().add(rootChild);
        loadChildData(rootChild, NetworkService.getEntityTree(),NetworkService.getEntityTree().getRoot());

        setMouseDoubleClickEvent();
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

            TreeItem<Text> child = createTreeItem(act.getID().toString(),UserID.getUserID().equals(act.getID()),
                    UserID.getUserID().equals(act.getID()),act.getID().toString());
            viewRoot.getChildren().add(child);
            loadChildData(child,entityTree,act);
        }
    }

    @FXML
    private TreeItem<Text> rootItem;
    @FXML
    private TreeView treeView;

}
