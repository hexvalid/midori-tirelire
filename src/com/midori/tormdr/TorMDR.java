package com.midori.tormdr;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TorMDR {
    private String binaryPath;
    private String dataDirectory;

    private int no;

    private int socksPort;
    private int controlPort;

    private boolean hardwareAccel;
    private int keepAlivePeriod;
    private boolean useSocks5Proxy;
    private String socks5ProxyAddress;
    private String socks5ProxyUserName;
    private String socks5ProxyPassword;
    private boolean useObfs4Proxy;
    private String obfs4ProxyPath;
    private List<String> obfs4Bridges;


    private Process process;
    private InputStream inputStream;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;


    public TorMDR(int no, String binaryPath, String dataDirectory) {
        this.no = no;
        this.socksPort = socksPortStart + no;
        this.controlPort = controlPortStart + no;
        this.binaryPath = binaryPath;
        this.dataDirectory = dataDirectory;


    }

    public static void main(String[] args) throws Exception {
        List<String> launchParams = new ArrayList<>(defaultArgs);
        launchParams.add(0, "/usr/bin/tormdr");
        Process process = new ProcessBuilder(launchParams).start();
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);


        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }


    }

    final private static String javaLibVersion = "0.1-portOfGo=1.0S";
    final private static String localhost = "127.0.0.1";
    final private static int socksPortStart = 20000;
    final private static int controlPortStart = 40000;
    final private static List<String> defaultArgs = Arrays.asList(
            "RunAsDaemon", "0",
            "ClientOnly", "1",
            "AvoidDiskWrites", "1",
            "FetchHidServDescriptors", "0",
            "FetchServerDescriptors", "1",
            "FetchUselessDescriptors", "0",
            "UseEntryGuards", "0",
            "NumEntryGuards", "0",
            "UseGuardFraction", "0",
            "DownloadExtraInfo", "0",
            "UseMicrodescriptors", "1",
            "ClientUseIPv4", "1",
            "ClientUseIPv6", "0",
            "DirCache", "0",
            "NewCircuitPeriod", "90 days",
            "MaxCircuitDirtiness", "30 days",
            "EnforceDistinctSubnets", "0");
}


