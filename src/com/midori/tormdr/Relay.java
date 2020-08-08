package com.midori.tormdr;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Relay {

    final private SimpleStringProperty IP;
    final private SimpleIntegerProperty bandwidth;
    final private SimpleStringProperty fast;
    final private SimpleStringProperty stable;
    private SimpleIntegerProperty ping;
    private SimpleIntegerProperty comment;
    private SimpleIntegerProperty countryCode;


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
}