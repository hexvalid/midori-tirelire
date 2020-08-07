package com.midori.utils;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;

public class Rate {
    final private static String URL_BASE = "https://api.exchangeratesapi.io/latest";

    public static double getRate(String base, String target) throws URISyntaxException, IOException {
        final URIBuilder uriB = new URIBuilder(URL_BASE);
        uriB.addParameter("base", base);
        uriB.addParameter("symbols", target);
        HttpRequestBase req = new HttpGet(uriB.build());
        return new JSONObject(HttpTools.executeMiniRequest(req)).getJSONObject("rates").getDouble(target);
    }
}
