package com.sharedtable.controller;

import com.sharedtable.model.network.NetworkClientEntity;
import com.sharedtable.model.network.NetworkService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientPropertyWindowController implements Initializable {

    private NetworkClientEntity entity;
    @FXML
    private Label idLabel;
    @FXML
    private Label nicknameLabel;
    @FXML
    private Label IPLabel;
    @FXML
    private Label portLabel;
    @FXML
    private Label parentIDLabel;
    @FXML
    private Label pingLabel;
    @FXML
    private Button pingButton;

    class PingThread extends Thread {

        public PingThread(){
            start();
        }

        @Override
        public void run(){
            disablePingButton();
            long sum = 0;
            long act = NetworkService.getInstance().pingClient(entity.getID());
            updatePingLabel(act);
            sum += act;
            act = NetworkService.getInstance().pingClient(entity.getID());
            updatePingLabel(act);
            sum += act;
            act = NetworkService.getInstance().pingClient(entity.getID());
            updatePingLabel(act);
            sum += act;
            act = NetworkService.getInstance().pingClient(entity.getID());
            updatePingLabel(act);
            sum += act;

            reEnablePingButton();
            writeFinalPingResult(sum);
        }

        private void disablePingButton() {
            Platform.runLater(() -> {
                pingButton.setDisable(true);
                pingButton.setText("Pingelés...");
            });
        }

        private void updatePingLabel(long val) {
            final long actFinal = val;
            Platform.runLater(() -> {
                pingLabel.setText((double)actFinal/1000000+" ms");
            });
        }

        private void reEnablePingButton(){
            Platform.runLater(() -> {
                pingButton.setDisable(false);
                pingButton.setText("Pingelés");
            });
        }

        private void writeFinalPingResult(long val){
            Platform.runLater(() -> {
                pingLabel.setText(((double)val/1000000)/4+" ms átlag");
            });
        }


    }

    public ClientPropertyWindowController() {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void setData(NetworkClientEntity entity) {
        idLabel.setText(entity.getID().toString());
        nicknameLabel.setText(entity.getNickname());
        IPLabel.setText(entity.getIP());
        portLabel.setText(String.valueOf(entity.getPort()));
        if(entity.getUpperClientID() != null)
            parentIDLabel.setText(entity.getUpperClientID().toString());
        else
            parentIDLabel.setText("None");
        if(entity.getID().equals(UserID.getInstance().getUserID())) {
            pingButton.setDisable(true);
            pingLabel.setText("saját kliens nem pingelhető!");
        }
        this.entity= entity;
    }



    @FXML
    public void btnPingButtonClicked(ActionEvent actionEvent) {
        new PingThread();
    }




}
