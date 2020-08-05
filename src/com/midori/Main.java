package com.midori;

import com.midori.ui.Dash;
import com.midori.utils.Rate;
import javafx.application.Application;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {

       // Pool.RATE_USD_TRY = Rate.getRate("USD", "TRY");

        System.out.println(Pool.RATE_USD_TRY);

        Application.launch(Dash.class, args);
    }
}
