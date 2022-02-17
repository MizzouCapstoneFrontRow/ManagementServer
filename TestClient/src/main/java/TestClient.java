package main.java;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;

public class TestClient {

    public static void main(String[] args) {

        // Config
        final String host = "localhost";
        final int port = 45575;

        // Connect
        TestClient instance = new TestClient(host, port);
        instance.connect();

        // Main Loop
        boolean shouldEnd = false;
        while(!shouldEnd) {
            try {
                String userInput = (new BufferedReader(new InputStreamReader(System.in))).readLine();
                StringTokenizer tokenizer = new StringTokenizer(userInput);
                String command = tokenizer.nextToken();
                switch(command) {
                    case "disconnect":
                        shouldEnd = true;
                        break;
                    case "send":
                        if(tokenizer.countTokens() >=1) {
                            StringBuilder message = new StringBuilder();
                            Files.lines(Paths.get(tokenizer.nextToken())).forEach(message::append);
                            instance.send(message.toString());
                        }
                        else {
                            System.out.printf("Invalid parameters for command \"%s\"\n");
                        }
                        break;
                    default:
                        System.out.printf("Invalid Command: \"%s\"\n", command);
                }
            } catch (Throwable t) {
                System.out.println("Error: Encountered Exception while Parsing Command!");
                t.printStackTrace();
            }
        }

        // Disconnect
        instance.disconnect();
        System.out.println("Sent disconnect packet!");
        instance.close();

    }

    Socket socket;
    final String host;
    final int port;

    PrintWriter socketOutputStreamWriter;

    public TestClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        boolean connected = false;
        while(!connected) {
            try {
                socket = new Socket(host, port);
                socketOutputStreamWriter = new PrintWriter(socket.getOutputStream(), true);
                connected = true;
            } catch (IOException exception) {
                System.err.printf("[Error] Failed to connect to %s:%d, trying again in 5 seconds...\n", host, port);
            }
        }
        System.out.printf("Connected to %s:%d.\n", host, port);
    }

    public void disconnect() {
        send("{\"message_id\": 0, \"message_type\": \"disconnect_machine\"}");
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException exception) {
            System.err.println("[Error] Failed to Close Socket!");
        }
    }

    public void send(String toSend) {
        socketOutputStreamWriter.println(toSend);
    }

}
