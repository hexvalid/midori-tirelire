package com.midori.ui;

import com.midori.tormdr.Config;
import com.midori.tormdr.Relay;
import com.midori.tormdr.TorMDR;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.TextFlow;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ResourceBundle;

public class DashController implements Initializable {


    @FXML
    private Button _relayStartDiagnosticButton;

    @FXML
    private ProgressBar _relayDiagnosticProgress;

    @FXML
    private TableView<Relay> _relayList;

    @FXML
    private TableColumn<Relay, String> _relayList_IP;

    @FXML
    private TableColumn<Relay, String> _relayList_CT;

    @FXML
    private TableColumn<Relay, Integer> _relayList_BW;

    @FXML
    private TableColumn<Relay, Integer> _relayList_Ping;

    @FXML
    private TableColumn<Relay, String> _relayList_F;

    @FXML
    private TableColumn<Relay, String> _relayList_S;

    @FXML
    private TableColumn<Relay, String> _relayList_V;

    @FXML
    private TableColumn<Relay, String> _relayList_Comment;

    @FXML
    protected ListView<TextFlow> _logView;

    @FXML
    private Label _btc_rate;

    private TorMDR tormdr;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Log.Print(Log.t.INF, "Initiliaze started");
        _relayList_CT.setCellValueFactory(new PropertyValueFactory<>("countryCode"));
        _relayList_IP.setCellValueFactory(new PropertyValueFactory<>("IP"));
        _relayList_BW.setCellValueFactory(new PropertyValueFactory<>("bandwidth"));
        _relayList_V.setCellValueFactory(new PropertyValueFactory<>("valid"));
        _relayList_Ping.setCellValueFactory(new PropertyValueFactory<>("ping"));
        _relayList_F.setCellValueFactory(new PropertyValueFactory<>("fast"));
        _relayList_S.setCellValueFactory(new PropertyValueFactory<>("stable"));


        MenuItem mi1 = new MenuItem("Measure Ping");
        mi1.setOnAction((ActionEvent event) -> {
            new Thread(() -> {
                Relay item = _relayList.getSelectionModel().getSelectedItem();


                try {
                    tormdr.SetRelay(item.getIP());
                    item.checkValid(tormdr);
                    Platform.runLater(() -> _relayList.refresh());

                    item.checkCountryCode(tormdr);
                    Platform.runLater(() -> _relayList.refresh());

                    item.checkPing(tormdr);
                    Platform.runLater(() -> _relayList.refresh());
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }).start();
        });


        ContextMenu menu = new ContextMenu();
        menu.getItems().add(mi1);
        _relayList.setContextMenu(menu);

    }

    @FXML
    public void startRelayDiagnostic() {

        new Thread(() -> {
            Platform.runLater(() -> _relayStartDiagnosticButton.setDisable(true));

            try {
                Config config = new Config();
                config.dataDirectory = "/tmp/tormdr";
                config.binaryPath = "/usr/bin/tormdr";
/*                config.useSocks5Proxy = true;
                config.socks5ProxyAddress = "40.70.243.118:32416";
                config.socks5ProxyUserName = "e4cf6e290c0cf8ae8fb91fcf818e1e40";
                config.socks5ProxyPassword = "a565ab1f3802afbf4d07c1674069d813";*/

                tormdr = new TorMDR(1, config);
                tormdr.Start();
                final ObservableList<Relay> data = FXCollections.observableArrayList(Relay.parseRelayList());
                _relayList.setItems(data);

                Platform.runLater(() -> _relayStartDiagnosticButton.setDisable(false));

            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }
}
