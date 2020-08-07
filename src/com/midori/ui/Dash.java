package com.midori.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Dash extends Application {
    final private static int fontSize = 12;
    static DashController Controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Log.PRINT_UI = true;
        System.setProperty("prism.allowhidpi", "false");
        setUserAgentStylesheet(STYLESHEET_MODENA);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("dash.fxml"));
        Font.loadFont(getClass().getResource("res/fonts/fontello.ttf").toExternalForm(), fontSize);
        Font.loadFont(getClass().getResource("res/fonts/Roboto-Regular.ttf").toExternalForm(), fontSize);
        Font.loadFont(getClass().getResource("res/fonts/Roboto-Italic.ttf").toExternalForm(), fontSize);
        Font.loadFont(getClass().getResource("res/fonts/Roboto-Bold.ttf").toExternalForm(), fontSize);
        Font.loadFont(getClass().getResource("res/fonts/Roboto-Bold.ttf").toExternalForm(), fontSize);
        Font.loadFont(getClass().getResource("res/fonts/RobotoCondensed-Regular.ttf").toExternalForm(), fontSize);
        Font.loadFont(getClass().getResource("res/fonts/RobotoCondensed-Bold.ttf").toExternalForm(), fontSize);
        Font.loadFont(getClass().getResource("res/fonts/RobotoMono-Regular.ttf").toExternalForm(), fontSize);
        Font.loadFont(getClass().getResource("res/fonts/RobotoMono-Bold.ttf").toExternalForm(), fontSize);
        Parent root = loader.load();
        root.getStylesheets().add(getClass().getResource("res/dash.css").toExternalForm());
        Controller = loader.getController();
        primaryStage.setResizable(false);
        primaryStage.setFullScreen(false);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

}
