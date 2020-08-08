package com.midori.tormdr;

import java.io.*;
import java.net.Socket;

public class Control {

    final protected static String MSG_AUTHENTICATE = "AUTHENTICATE";
    final protected static String MSG_SETEXITNODES = "SETCONF ExitNodes=";
    final protected static String MSG_NEWNYM = "SIGNAL NEWNYM";
    final protected static String MSG_SHUTDOWN = "SIGNAL SHUTDOWN";

    private Socket socket;
    private OutputStream streamOut;
    private InputStream streamIn;
    private PrintStream streamPrint;
    private BufferedReader reader;

    protected Control(int controlPort) throws IOException {
        this.socket = new Socket(TorMDR.localhost, controlPort);
        this.streamOut = socket.getOutputStream();
        this.streamIn = socket.getInputStream();
        this.streamPrint = new PrintStream(streamOut, true);
        this.reader = new BufferedReader(new InputStreamReader(streamIn));
    }


    protected void SendMessage(String cmd) throws IOException {
        this.streamPrint.println(cmd);
        String response = this.reader.readLine();
        if (!response.equals("250 OK")) {
            throw new IOException("Unexcepted response from control signal: " + response);
        }
    }

    protected void Close() throws IOException {
        this.reader.close();
        this.socket.close();
        this.streamPrint.close();
        this.streamIn.close();
        this.streamOut.close();
    }
}
