package com.midori.utils;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class HttpTools {

    public static CloseableHttpClient miniHttpClient = HttpClients.custom()
            .disableAuthCaching()
            .disableConnectionState()
            .disableRedirectHandling()
            .disableCookieManagement()
            .disableDefaultUserAgent()
            .disableContentCompression()
            .disableAutomaticRetries()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setConnectTimeout(30 * 1000)
                    .setConnectionRequestTimeout(30 * 1000)
                    .setSocketTimeout(32 * 1000).build())
            .setSSLContext(dummySSLContext())
            .build();

    private static SSLContext dummySSLContext() {
        try {
            return SSLContexts.custom().loadTrustMaterial((chain, authType) -> true).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
            return null;
        }
    }
}
