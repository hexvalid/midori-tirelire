package com.midori.tormdr;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Relay {
    final private static String CHECK_IP_URL = "http://checkip.amazonaws.com/";
    final private static String CHECK_PING_URL = "https://static1.freebitco.in/images/100.png";
    final private static String CHECK_CT_URL = "https://freebitco.in/?op=signup_page";


    final private static int PING_TEST_COUNT = 3;

    final private SimpleStringProperty IP;
    final private SimpleIntegerProperty bandwidth;
    private SimpleStringProperty valid;
    final private SimpleStringProperty fast;
    final private SimpleStringProperty stable;
    private SimpleIntegerProperty ping;
    private SimpleIntegerProperty comment;
    private SimpleStringProperty countryCode;


    private Relay(String ip, int bandwidth, boolean fast, boolean stable) {
        this.IP = new SimpleStringProperty(ip);
        this.bandwidth = new SimpleIntegerProperty(bandwidth);
        if (fast) {
            this.fast = new SimpleStringProperty("⚫");
        } else {
            this.fast = new SimpleStringProperty();
        }
        if (stable) {
            this.stable = new SimpleStringProperty("⚫");
        } else {
            this.stable = new SimpleStringProperty();
        }
        this.ping = new SimpleIntegerProperty();
        this.valid = new SimpleStringProperty();
        this.countryCode = new SimpleStringProperty();
    }

    public String getIP() {
        return this.IP.get();
    }

    public Integer getBandwidth() {
        return this.bandwidth.get();
    }

    public String getFast() {
        return this.fast.get();
    }

    public String getStable() {
        return this.stable.get();
    }


    public String getValid() {
        return this.valid.get();
    }

    public int getPing() {
        return this.ping.get();
    }

    public String getCountryCode() {
        return this.countryCode.get();
    }

    public void setPing(int ping) {
        this.ping.set(ping);
    }

    public void setValid(boolean status) {
        if (status) {
            this.valid.set("⚫");
        } else {
            this.valid.set("");
        }
    }


    public void setCountryCode(String countryCode) {
        this.countryCode.set(countryCode);
    }

    public static List<Relay> parseRelayList() throws IOException {
        List<Relay> relayList = new ArrayList<>();
        Scanner scanner = new Scanner(new File("/tmp/tormdr/1/cache/cached-microdesc-consensus"));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.charAt(0) == 'r' && line.charAt(1) == ' ') {
                String ip = line.split(" ")[5];
                if (scanner.nextLine().charAt(0) == 'a') {
                    scanner.nextLine();
                }
                String lineS = scanner.nextLine();
                if (!(lineS.charAt(0) == 's' && lineS.charAt(1) == ' ')) {
                    throw new IOException("unexcepted S line");
                }
                scanner.nextLine();
                scanner.nextLine();
                if (lineS.contains("Exit") && lineS.contains("Running") && lineS.contains("Valid")
                        && !lineS.contains("BadExit")) {
                    boolean fast = lineS.contains("Fast");
                    boolean stable = lineS.contains("Stable");
                    String lineW = scanner.nextLine();
                    if (!(lineW.charAt(0) == 'w' && lineW.charAt(1) == ' ' && lineW.charAt(2) == 'B')) {
                        throw new IOException("unexcepted W line");
                    }
                    int bandwidth;
                    if (lineW.contains("Unmeasured")) {
                        bandwidth = -1;
                    } else {
                        bandwidth = Integer.parseInt(lineW.split("=")[1]);
                    }
                    relayList.add(new Relay(ip, bandwidth, fast, stable));
                } else {
                    scanner.nextLine();
                }
            }
        }
        scanner.close();
        return relayList;
    }

    public void checkValid(TorMDR tormdr) {
        try {
            String ip = tormdr.ExecuteRequest(new HttpGet(CHECK_IP_URL)).trim();
            this.setValid(ip.trim().equals(this.IP.get()));
        } catch (IOException e) {
            this.setValid(false);
        }
    }

    public void checkPing(TorMDR tormdr) {
        try {
            String[] fragments = new String[PING_TEST_COUNT];
            long totalElapsed = 0;
            for (int i = 0; i < PING_TEST_COUNT; i++) {
                Instant start = Instant.now();
                fragments[i] = tormdr.ExecuteRequest(new HttpGet(CHECK_PING_URL));
                Instant finish = Instant.now();
                totalElapsed += Duration.between(start, finish).toMillis();
            }
            this.setPing((int) totalElapsed / PING_TEST_COUNT);
            if (!(fragments[0].equals(fragments[1]) && fragments[1].equals(fragments[2]))) {
                this.setValid(false);
            }
        } catch (IOException e) {
            this.setValid(false);
        }
    }

    public void checkCountryCode(TorMDR tormdr) {
        try {
            String ct = tormdr.ExecuteRequest(new HttpGet(CHECK_CT_URL));

            this.setCountryCode(StringUtils.substringBetween(ct,"country = '","'"));
        } catch (IOException e) {
            this.setCountryCode("??");
        }
    }
}