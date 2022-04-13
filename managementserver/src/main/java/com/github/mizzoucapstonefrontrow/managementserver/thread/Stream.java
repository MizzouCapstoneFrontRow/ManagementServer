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
        try {
            while (true) {

                if (connectionReceiving == null || connectionSending == null) {
                    wait();
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
        } catch (InterruptedException ignored) {}
    }

    /**
     * Attempts to assign receiving connection - only allowed if value was previously null
     * @return successful?
     */
    public boolean trySetConnectionReceiving(Socket connectionReceiving) {
        if(this.connectionReceiving == null) {
            this.connectionReceiving = connectionReceiving;
            notify();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Attempts to assign sending connection - only allowed if value was previously null
     * @return successful?
     */
    public boolean trySetConnectionSending(Socket connectionSending) {
        if(this.connectionSending == null) {
            this.connectionSending = connectionSending;
            notify();
            return true;
        } else {
            return false;
        }
    }

    public void close() {
        connectionReceiving = null;
        connectionSending = null;
        interrupt();
    }

}
