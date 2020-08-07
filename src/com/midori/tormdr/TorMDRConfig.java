package com.midori.tormdr;

import java.util.List;

public class TorMDRConfig {
    private String binaryPath;
    private String dataDirectory;
    private boolean hardwareAccel;
    private int keepAlivePeriod;
    private boolean useSocks5Proxy;
    private String socks5ProxyAddress;
    private String socks5ProxyUserName;
    private String socks5ProxyPassword;
    private boolean useObfs4Proxy;
    private String obfs4ProxyPath;
    private List<String> obfs4Bridges;
}
