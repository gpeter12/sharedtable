package com.sharedtable.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;

public class MainView {

    public MainView() {}

    public static Pair<Scene,Stage> initMainView(Stage primaryStage) {
        Parent root;
        try {
            root = FXMLLoader.load(MainView.class.getResource("MainView.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        primaryStage.setTitle("Shared Table");
        Scene scene = new Scene(root, 640, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
        Pair<Scene,Stage> res = new Pair<>(scene,primaryStage);
        return res;
    }
}
