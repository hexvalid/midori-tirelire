package com.midori.tormdr;

import com.midori.ui.Log;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class TorMDR {
    private final int no;
    private int socksPort;
    private int controlPort;
    private ProcessBuilder processBuilder;
    private Process process;
    private InputStream inputStream;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;
    private String bootStatus;


    public TorMDR(int no, Config config) throws IOException {
        Log.Print(Log.t.INF, String.format("TorMDR(%d): Initializing...", no));
        if (!new File(config.binaryPath).isFile()) {
            throw new NoSuchFileException(config.binaryPath);
        }
        List<String> params = new ArrayList<>(defaultArgs);
        params.add(0, config.binaryPath);
        this.no = no;
        params.add("SocksPort");
        this.socksPort = socksPortStart + no;
        params.add(String.valueOf(this.socksPort));
        params.add("ControlPort");
        this.controlPort = controlPortStart + no;
        params.add(String.valueOf(this.controlPort));
        params.add("DataDirectory");
        Path dataDir = Paths.get(config.dataDirectory, String.valueOf(no));
        Files.createDirectories(dataDir);
        Files.setPosixFilePermissions(dataDir, PosixFilePermissions.fromString(permMode));
        params.add(dataDir.toString());
        params.add("CacheDirectory");
        Path cacheDir = Paths.get(dataDir.toString(), "cache");
        Files.createDirectories(cacheDir);
        Files.setPosixFilePermissions(cacheDir, PosixFilePermissions.fromString(permMode));
        params.add(cacheDir.toString());
        if (config.keepAlivePeriod > 0) {
            params.add("KeepalivePeriod");
            params.add(String.valueOf(config.keepAlivePeriod));
        }
        params.add("HardwareAccel");
        if (config.hardwareAccel) {
            params.add("1");
        } else {
            params.add("0");
        }
        if (config.useSocks5Proxy) {
            params.add("Socks5Proxy");
            params.add(config.socks5ProxyAddress);
            params.add("Socks5ProxyUserName");
            params.add(config.socks5ProxyUserName);
            params.add("Socks5ProxyPassword");
            params.add(config.socks5ProxyPassword);
        }

        this.processBuilder = new ProcessBuilder(params);
    }

    public void Start() throws IOException {
        this.process = this.processBuilder.start();
        this.inputStream = process.getInputStream();
        this.inputStreamReader = new InputStreamReader(this.inputStream);
        this.bufferedReader = new BufferedReader(this.inputStreamReader);
        String versionLine = this.bufferedReader.readLine();
        if (!versionLine.contains("TorMDR=")) {
            throw new IOException("TorMDR is responded unexpectedly");
        }

        String[] versionParts = versionLine.split(" ");
        Log.Print(Log.t.DBG, String.format("%s(%d): %s started with %s %s, %s %s and %s %s.",
                versionParts[1].split("=")[0], this.no, versionParts[1].split("=")[1],
                versionParts[2].split("=")[0], versionParts[2].split("=")[1],
                versionParts[3].split("=")[0], versionParts[3].split("=")[1],
                versionParts[4].split("=")[0], versionParts[4].split("=")[1]));

        String line;
        while ((line = this.bufferedReader.readLine()) != null) {
            if (line.contains("Bootstrapped")) {
                this.bootStatus = StringUtils.substringBetween(line, "(", ")");
                Log.Print(Log.t.DBG, String.format("TorMDR(%d): Boot: %s", this.no, this.bootStatus));
                if (this.bootStatus.equals("done")) {
                    break;
                }
            } else if (line.contains("[warn]")) {
                Log.Print(Log.t.WRN, String.format("TorMDR(%d): " +
                        "%s", this.no, line.split("\\[warn\\]")[1].trim()));
            } else if (line.contains("[err]")) {
                Log.Print(Log.t.ERR, String.format("TorMDR(%d): %s", this.no, line.split("\\[err\\]")[1].trim()));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Log.PRINT_CONSOLE = true;





/*        Config config = new Config();
        config.dataDirectory = "/tmp/tormdr";
        config.binaryPath = "/usr/bin/tormdr";
        TorMDR tormdr = new TorMDR(1, config);
        tormdr.Start();*/


    }

    final private static String javaLibVersion = "0.1-portOfGo=1.0S";
    final private static String localhost = "127.0.0.1";
    final private static String permMode = "rwx------";

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


