package com.midori;

import com.midori.ui.Dash;
import com.midori.utils.HttpTools;
import javafx.application.Application;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        HttpGet httpget = new HttpGet("http://httpbin.org/get");
        System.out.println("Executing request " + httpget.getRequestLine());

        ResponseHandler<String> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };
        String responseBody =         HttpTools.miniHttpClient.execute(httpget,responseHandler);
        System.out.println("----------------------------------------");
        System.out.println(responseBody);

        System.exit(1);
        Application.launch(Dash.class, args);
    }
}
