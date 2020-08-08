package com.midori.tormdr;

import com.midori.ui.Log;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TorMDR {
    private final int no;
    private final int socksPort;
    private final int controlPort;
    private final ProcessBuilder processBuilder;
    private Process process;
    private HttpClientContext context;

    public TorMDR(int no, Config config) throws IOException {
        Log.Print(Log.t.INF, String.format("TorMDR(%d): v%s Initializing...", no, javaLibVersion));
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
        this.context = HttpClientContext.create();
    }

    public void Start() throws IOException {
        this.process = this.processBuilder.start();
        InputStream inputStream = process.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String versionLine = bufferedReader.readLine();
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
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("Bootstrapped")) {
                String bootStatus = StringUtils.substringBetween(line, "(", ")");
                Log.Print(Log.t.DBG, String.format("TorMDR(%d): Boot: %s", this.no, bootStatus));
                if (bootStatus.equals("done")) {
                    break;
                }
            } else if (line.contains("[warn]")) {
                Log.Print(Log.t.WRN, String.format("TorMDR(%d): " +
                        "%s", this.no, line.split("\\[warn\\]")[1].trim()));
            } else if (line.contains("[err]")) {
                Log.Print(Log.t.ERR, String.format("TorMDR(%d): %s", this.no, line.split("\\[err\\]")[1].trim()));
            }
        }
        this.context.setAttribute("socks.address", new InetSocketAddress(TorMDR.localhost, this.socksPort));
    }


    public void NewCircuit() throws IOException, InterruptedException {
        Log.Print(Log.t.DBG, String.format("TorMDR(%d): Building new circuit...", this.no));
        Control control = new Control(this.controlPort);
        control.SendMessage(Control.MSG_AUTHENTICATE);
        control.SendMessage(Control.MSG_NEWNYM);
        control.Close();
    }

    public void SetRelay(String relayIP) throws IOException {
        Log.Print(Log.t.DBG, String.format("TorMDR(%d): Setting relay: %s...", this.no, relayIP));
        Control control = new Control(this.controlPort);
        control.SendMessage(Control.MSG_AUTHENTICATE);
        control.SendMessage(Control.MSG_SETEXITNODES + relayIP);
        control.Close();
    }


    public void Stop() throws IOException {
        Log.Print(Log.t.DBG, String.format("TorMDR(%d): Stopping...", this.no));
        Control control = new Control(this.controlPort);
        control.SendMessage(Control.MSG_AUTHENTICATE);
        control.SendMessage(Control.MSG_SHUTDOWN);
        control.Close();
        this.process.destroy();
    }

    public HttpClientContext getContext() {
        return this.context;
    }

    public String ExecuteRequest(HttpRequestBase req) throws IOException {
        CloseableHttpClient client = HttpClients.custom()
                .disableAuthCaching()
                .disableConnectionState()
                .disableRedirectHandling()
                .disableCookieManagement()
                .disableDefaultUserAgent()
                .disableContentCompression()
                .disableAutomaticRetries()
                .setConnectionManager(SocksFactory.CreateConnectionManager())
                .build();
        CloseableHttpResponse res = client.execute(req, getContext());
        HttpEntity entity = res.getEntity();
        if (!(res.getStatusLine().getStatusCode() >= 200 &&
                res.getStatusLine().getStatusCode() < 300) || entity == null) {
            throw new ClientProtocolException("Wrong entity. Status: " + res.getStatusLine().getStatusCode());
        }
        String body = EntityUtils.toString(entity);
        res.close();
        client.close();
        return body;
    }

    public static void main(String[] args) throws Exception {
        Log.PRINT_CONSOLE = true;


        Config config = new Config();
        config.dataDirectory = "/tmp/tormdr";
        config.binaryPath = "/usr/bin/tormdr";
        TorMDR tormdr = new TorMDR(1, config);
        tormdr.Start();

        System.out.println(tormdr.ExecuteRequest(new HttpGet("http://checkip.amazonaws.com/")));

        tormdr.NewCircuit();
        System.out.println(tormdr.ExecuteRequest(new HttpGet("http://checkip.amazonaws.com/")));
        tormdr.NewCircuit();
        System.out.println(tormdr.ExecuteRequest(new HttpGet("http://checkip.amazonaws.com/")));
        tormdr.NewCircuit();
        System.out.println(tormdr.ExecuteRequest(new HttpGet("http://checkip.amazonaws.com/")));
        tormdr.NewCircuit();
        System.out.println(tormdr.ExecuteRequest(new HttpGet("http://checkip.amazonaws.com/")));

        tormdr.Stop();

    }

    final protected static String localhost = "127.0.0.1";
    final private static String javaLibVersion = "1.0-portOfGo=1.0S";
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


