package com.midori.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.ResourceBundle;

public class DashController implements Initializable {

    @FXML
    private TableView _relayList;

    @FXML
    public ListView<TextFlow> _logView;

    @FXML
    private Label _btc_rate;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
