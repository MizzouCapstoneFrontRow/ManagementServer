package com.github.mizzoucapstonefrontrow.managementserver.thread;

import com.github.mizzoucapstonefrontrow.managementserver.server.Console;

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

    private InputStream connectionReceivingInputStream = null;
    private InputStream connectionSendingInputStream = null; // Used during handshake to receive stream descriptor
    private OutputStream outputStream = null;

    public static String[] findStreamMachineAndName(Stream s) {
        for(String machineName : streams.keySet()) {
            HashMap<String, Stream> streamsForMachine = streams.get(machineName);
            for(String streamName : streamsForMachine.keySet()) {
                if(streamsForMachine.get(streamName).equals(s)) {
                    return new String[] {machineName, streamName};
                }
            }
        }
        return null;
    }

    public Stream() {
        Console.log("Instantiating Stream!");
    }

    @Override
    public void run() {
        while (true) {

            if (connectionReceiving == null || connectionSending == null) {
                Console.log("Stream has a null connection!");
                synchronizedWait();
            }

            if (connectionReceiving == null || connectionReceiving.isClosed() || connectionReceivingInputStream == null) {
                connectionReceiving = null;
                if(connectionReceivingInputStream != null) {
                    try { connectionReceivingInputStream.close(); } catch (IOException ignored) {}
                    connectionReceivingInputStream = null;
                }
                Console.format("Receiving Connection for Stream [%s] either missing or improperly created - waiting on reconnection from Machine to initiate stream.", this);
                continue;
            }

            if (connectionSending == null || connectionSending.isClosed() || outputStream == null) {
                connectionSending = null;
                if(connectionSendingInputStream != null) {
                    try { connectionSendingInputStream.close(); } catch (IOException ignored) {}
                    connectionSendingInputStream = null;
                }
                if(outputStream != null) {
                    try { outputStream.close(); } catch (IOException ignored) {}
                    outputStream = null;
                }
                Console.format("Sending Connection for Stream [%s] either missing or improperly created - waiting on reconnection from User Environment to initiate stream.", this);
                continue;
            }

            try {
                connectionReceivingInputStream.transferTo(outputStream);
            } catch (IOException e) {
                String[] streamInfo = findStreamMachineAndName(this);
                Console.format("Caught IOException when Forwarding a Stream! This might not be an error, and is expected if either the Machine or the User Environment disconnects. Stream Info: %s.", this);
                if(streamInfo == null) e.printStackTrace(Console.out());
            }

        }
    }

    @FunctionalInterface
    public interface ConnectionAdditionFunction {
        boolean setConnection(Stream instance, Socket connection, InputStream inputStream);
    }

    /**
     * Attempts to assign receiving connection - only allowed if value was previously null
     * @return successful?
     */
    public synchronized boolean trySetConnectionReceiving(Socket connectionReceiving, InputStream connectionReceivingInputStream) {
        if(this.connectionReceiving == null) {
            this.connectionReceiving = connectionReceiving;
            this.connectionReceivingInputStream = connectionReceivingInputStream;
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
    public synchronized boolean trySetConnectionSending(Socket connectionSending, InputStream connectionSendingInputStream) {

        if(this.connectionSending == null) {
            this.connectionSending = connectionSending;
            try {
                this.outputStream = connectionSending.getOutputStream();
            } catch (IOException e) {
                Console.log("Failed to Get Output Stream for Outgoing Stream Connection");
                this.connectionSending = null;
                this.outputStream = null;
                return false;
            }
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
        catch (Throwable t) { t.printStackTrace(Console.out()); }
    }

    public synchronized void synchronizedNotify() {
        try {
            notify();
        } catch (Throwable t) {
            t.printStackTrace(Console.out());
        }
    }

    public void close() {
        connectionReceiving = null;
        connectionSending = null;
        interrupt();
    }

    @Override
    public String toString() {
        String[] streamInfo = findStreamMachineAndName(this);
        return streamInfo == null ? "Unknown" : String.format(
                "Machine Name = \"%s\", Stream Name = \"%s\"",
                streamInfo[0],
                streamInfo[1]
        );
    }

}
