package com.github.mizzoucapstonefrontrow.managementserver.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 * Re-hosts incoming byte stream from machine
 */
public class Stream extends Thread {

    // HashMap<machineName, HashMap<streamName, stream>>
    public static HashMap<String, HashMap<String, Stream>> streams = new HashMap<>();

    private Socket connectionReceiving = null;
    private Socket connectionSending = null;

    public Stream() {}

    @Override
    public void run() {
        while (true) {

            if (connectionReceiving == null || connectionSending == null) {
                synchronizedWait();
            }

            if (connectionReceiving.isClosed()) {
                connectionReceiving = null;
                continue;
            }

            if (connectionSending.isClosed()) {
                connectionSending = null;
                continue;
            }

            InputStream inputStream = null;
            try {
                inputStream = connectionReceiving.getInputStream();
            } catch (Throwable t) {
                connectionReceiving = null;
                continue;
            }

            OutputStream outputStream = null;
            try {
                outputStream = connectionSending.getOutputStream();
            } catch (Throwable t) {
                connectionSending = null;
                continue;
            }

            try {
                inputStream.transferTo(outputStream);
            } catch (IOException ignored) {}

        }
    }

    /**
     * Attempts to assign receiving connection - only allowed if value was previously null
     * @return successful?
     */
    public synchronized boolean trySetConnectionReceiving(Socket connectionReceiving) {
        if(this.connectionReceiving == null) {
            this.connectionReceiving = connectionReceiving;
            synchronizedNotify();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Attempts to assign sending connection - only allowed if value was previously null
     * @return successful?
     */
    public synchronized boolean trySetConnectionSending(Socket connectionSending) {
        if(this.connectionSending == null) {
            this.connectionSending = connectionSending;
            synchronizedNotify();
            return true;
        } else {
            return false;
        }
    }

    public synchronized void synchronizedWait() {
        try {
            wait();
        }
        catch (InterruptedException ignored) {}
        catch (Throwable t) { t.printStackTrace(); }
    }

    public synchronized void synchronizedNotify() {
        try {
            notify();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void close() {
        connectionReceiving = null;
        connectionSending = null;
        interrupt();
    }

}
