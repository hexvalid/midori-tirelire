package com.midori.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;

public class HttpTools {

    public static CloseableHttpClient miniHttpClient = HttpClients.custom()
            .disableAuthCaching()
            .disableConnectionState()
            .disableRedirectHandling()
            .disableCookieManagement()
            .disableDefaultUserAgent()
            .disableContentCompression()
            .disableAutomaticRetries()
            .setDefaultRequestConfig(miniRequestManager(15 * 1000))
            .setConnectionManager(miniConnManager(1, 2))
            .build();

    private static PoolingHttpClientConnectionManager miniConnManager(int maxPerRoute, int maxTotal) {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }
            }}, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        SSLConnectionSocketFactory sslSocketFactory =
                new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        registryBuilder.register("http", PlainConnectionSocketFactory.getSocketFactory());
        registryBuilder.register("https", sslSocketFactory);
        Registry<ConnectionSocketFactory> registry = registryBuilder.build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);
        connectionManager.setMaxTotal(maxTotal);
        return connectionManager;
    }

    private static RequestConfig miniRequestManager(int timeout) {
        return RequestConfig.custom().setSocketTimeout(timeout)
                .setConnectTimeout(timeout).setConnectionRequestTimeout(timeout)
                .setContentCompressionEnabled(false).build();
    }

    public static String executeMiniRequest(HttpRequestBase req) throws IOException {
        CloseableHttpResponse res = HttpTools.miniHttpClient.execute(req);
        int statusCode = res.getStatusLine().getStatusCode();
        if (!(statusCode >= 200 && statusCode < 300)) {
            throw new ClientProtocolException("Unexpected response status: " + statusCode);
        }
        HttpEntity entity = res.getEntity();
        if (entity == null) {
            throw new ClientProtocolException("Null entity");
        }
        String body = EntityUtils.toString(entity);
        res.close();
        return body;
    }
}
