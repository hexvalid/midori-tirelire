package com.midori.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
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
        CloseableHttpResponse res = HttpTools.miniHttpClient.execute(req);
        int statusCode = res.getStatusLine().getStatusCode();
        if (!(statusCode >= 200 && statusCode < 300)) {
            throw new ClientProtocolException("Unexpected response status: " + statusCode);
        }
        HttpEntity entity = res.getEntity();
        if (entity == null) {
            throw new ClientProtocolException("Null entity");
        }
        double rate = new JSONObject(EntityUtils.toString(entity)).getJSONObject("rates").getDouble(target);
        res.close();
        return rate;
    }
}
