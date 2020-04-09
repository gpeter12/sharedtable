package com.sharedtable.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class RenameTabController implements Initializable {


    @FXML
    public void btnCreateNewTabClicked(ActionEvent actionEvent) {
        tabName = tabNameTextField.getText();
        isCanceled = false;
        closeStage(actionEvent);
    }
    @FXML
    public void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            tabName = tabNameTextField.getText();
            isCanceled = false;

            Node source = (Node) keyEvent.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }
    }
    public String getTabName() {return tabName;}

    public boolean isCanceled() {return isCanceled;}

    private void closeStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    @FXML
    private TextField tabNameTextField;
    private String tabName;
    private boolean isCanceled = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
