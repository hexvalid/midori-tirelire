package com.midori.ui;

import com.midori.Pool;
import com.midori.utils.HttpTools;
import com.midori.utils.Rate;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.TextFlow;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;

public class DashController implements Initializable {


    @FXML
    private Button _relayStartDiagnosticButton;

    @FXML
    private ProgressBar _relayDiagnosticProgress;

    @FXML
    private TableView<?> _relayList;

    @FXML
    public ListView<TextFlow> _logView;

    @FXML
    private Label _btc_rate;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Log.Print(Log.t.INF, "Initiliaze started");


    }

    @FXML
    public void startRelayDiagnostic() {


    }
}
