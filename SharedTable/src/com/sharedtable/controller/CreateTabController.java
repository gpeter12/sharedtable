package com.sharedtable.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class CreateTabController {


    @FXML
    public void btnCreateNewTabClicked(ActionEvent actionEvent) {
        tabName = tabNameTextField.getText();
        closeStage(actionEvent);
    }
    @FXML
    public void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            tabName = tabNameTextField.getText();

            Node source = (Node) keyEvent.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }
    }
    public String getTabName() {return tabName;}

    private void closeStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    @FXML
    private TextField tabNameTextField;
    private String tabName;

}
