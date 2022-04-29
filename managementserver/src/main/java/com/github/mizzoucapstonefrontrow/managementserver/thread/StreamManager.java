package com.github.mizzoucapstonefrontrow.managementserver.thread;

import com.github.mizzoucapstonefrontrow.managementserver.server.Console;
import com.github.mizzoucapstonefrontrow.managementserver.server.Server;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Optional;

public class StreamManager extends Thread {

    public enum StreamSide {

        MACHINE("Machine", Stream::trySetConnectionReceiving),
        USER_ENVIRONMENT("User Environment", Stream::trySetConnectionSending);

        String friendlyName;
        Stream.ConnectionAdditionFunction connectionAdditionFunction;

        StreamSide(String friendlyName, Stream.ConnectionAdditionFunction connectionAdditionFunction) {
            this.friendlyName = friendlyName;
            this.connectionAdditionFunction = connectionAdditionFunction;
        }

        public String toString() {
            return friendlyName;
        }

    }

    private static StreamManager machineInstance = null;
    private static StreamManager userEnvironmentInstance = null;

    public static StreamManager getMachineInstance() {
        if (machineInstance == null) {
            machineInstance = new StreamManager(
                    Optional.ofNullable(Server.settings.getInt("machine_stream_port")).orElse(45577),
                    StreamSide.MACHINE
            );
        }
        return machineInstance;
    }

    public static StreamManager getUserEnvironmentInstance() {
        if (userEnvironmentInstance == null) {
            userEnvironmentInstance = new StreamManager(
                    Optional.ofNullable(Server.settings.getInt("user_environment_stream_port")).orElse(45578),
                    StreamSide.USER_ENVIRONMENT
            );
        }
        return userEnvironmentInstance;
    }

    private StreamManager(int port, StreamSide side) {

        this.port = port;
        this.side = side;

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (Throwable t) {
            Console.format("Failed to Instantiate ServerSocket for %s Stream Manager on Port #%s!", this.side, this.port);
        }
        this.serverSocket = serverSocket;

    }

    public final int port;
    public final StreamSide side;

    private final ServerSocket serverSocket;
    private Socket connection;

    public void run() {

        // Connection Acceptance Loop
        while(!serverSocket.isClosed()) {
            try {

                connection = serverSocket.accept();
                InputStream inputStream = connection.getInputStream();
                BufferedReader connectionReader = new BufferedReader(new InputStreamReader(inputStream));

                // Debug Print
                Console.format("Received Connection to Stream Manager from side \"%s\".", side);

                try {

                    // Parse JSON
                    JsonObject message = Server.json.fromJson(connectionReader.readLine(), JsonObject.class);
                    if (!message.get("message_type").getAsString().equals("stream_descriptor")) {
                        throw new JsonParseException("Invalid \"message_type\" - Expected \"stream_descriptor\"");
                    }
                    String machineName = message.get("machine").getAsString();
                    String streamName = message.get("stream").getAsString();

                    // Debug Print
                    Console.format("Receiving Stream Descriptor from side \"%s\". Machine = \"%s\", Stream = \"%s\".", side, machineName, streamName);

                    // Fetch or Create Stream
                    HashMap<String, Stream> streamsForMachine = Stream.streams.computeIfAbsent(machineName, k -> new HashMap<>());
                    Stream stream = streamsForMachine.computeIfAbsent(streamName, k -> new Stream());
                    try {
                        if(!stream.isAlive()) stream.start();
                    } catch (IllegalThreadStateException e) {
                        Console.format("Failed to Start %s Stream Thread!", side);
                        e.printStackTrace(Console.out());
                    }

                    // Add Connection to Stream
                    side.connectionAdditionFunction.setConnection(stream, connection, inputStream);
                    stream.synchronizedNotify();

                } catch (Throwable t) {
                    Console.format("Failed to Parse Stream Descriptor from %s!", side);
                    t.printStackTrace(Console.out());
                }
            } catch (Throwable ignored) {}
        }

        // Shutdown/Cleanup Routine (A lot of this is redundant - trying to be as thorough as possible)
        Console.format("Closed %s Stream Manager Thread", side);
        try {
            if(serverSocket != null) serverSocket.close();
        } catch (Throwable t) {
            Console.format("Failed to Close %s Stream ServerSocket!", side);
            t.printStackTrace(Console.out());
        }
        try {
            if(connection != null) connection.close();
        } catch (Throwable t) {
            Console.format("Failed to Close Pending %s Stream Connection!", side);
            t.printStackTrace(Console.out());
        }
        Stream.streams.forEach((machineName, streams) -> {
            streams.forEach((streamName, stream) -> {
                stream.close();
            });
            streams.replaceAll((streamName, stream) -> null);
        });
        Stream.streams.replaceAll((machineName, streams) -> null);
        Stream.streams = new HashMap<>();

    }

    public void shutdown() {
        try {
            serverSocket.close();
        } catch (Throwable t) {
            Console.format("Failed to Shutdown %s Stream Manager Thread!", side);
        }
    }

}
