package com.midori.tormdr;

import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SocksFactory {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final String SOCKS_ADDRESS_ATTRIBUTE = "socks.address";


    public static PoolingHttpClientConnectionManager CreateConnectionManager() {
        Registry<ConnectionSocketFactory> httpRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register(HTTP, new SocksFactory.Socks5ConnectionSocketFactory())
                .register(HTTPS, new SocksFactory.SSLSocks5ConnectionSocketFactory(SSLContexts.createSystemDefault()))
                .build();
        return new PoolingHttpClientConnectionManager(httpRegistry);
    }

    private static Socket buildSocket(final HttpContext context) {
        if (context.getAttribute(SOCKS_ADDRESS_ATTRIBUTE) == null)
            throw new IllegalStateException("Can not find attribute '" + SOCKS_ADDRESS_ATTRIBUTE + "' in context");
        InetSocketAddress socksAddress = (InetSocketAddress) context.getAttribute(SOCKS_ADDRESS_ATTRIBUTE);
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksAddress);
        return new Socket(proxy);
    }

    private static class Socks5ConnectionSocketFactory implements ConnectionSocketFactory {
        @Override
        public Socket createSocket(final HttpContext context) {
            return buildSocket(context);
        }

        @Override
        public Socket connectSocket(
                final int connectTimeout,
                final Socket socket,
                final HttpHost host,
                final InetSocketAddress remoteAddress,
                final InetSocketAddress localAddress,
                final HttpContext context) throws IOException {
            Socket sock;
            if (socket != null) {
                sock = socket;
            } else {
                sock = createSocket(context);
            }
            if (localAddress != null) {
                sock.bind(localAddress);
            }
            try {
                sock.connect(remoteAddress, connectTimeout);
            } catch (SocketTimeoutException ex) {
                throw new ConnectTimeoutException(ex, host, remoteAddress.getAddress());
            }
            return sock;
        }
    }

    private static class SSLSocks5ConnectionSocketFactory extends SSLConnectionSocketFactory {
        public SSLSocks5ConnectionSocketFactory(final SSLContext sslContext) {
            super(sslContext);
        }

        @Override
        public Socket createSocket(final HttpContext context) {
            return buildSocket(context);
        }
    }
}
