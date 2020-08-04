package com.midori.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Dash extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("dash.fxml"));
        Scene scene = new Scene(root);
        setUserAgentStylesheet(STYLESHEET_MODENA);
        scene.getStylesheets().add(Dash.class.getResource("res/dash.css").toExternalForm());
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
