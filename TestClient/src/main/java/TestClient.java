package main.java;

import java.io.*;
import java.net.Socket;

public class TestClient {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 45575;
        TestClient instance = new TestClient(host, port);
        instance.connect();
        try {
            Thread.sleep(5000);
        } catch (Throwable ignored) {}
        instance.disconnect();
    }

    Socket socket;
    String host;
    int port;

    public TestClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        boolean connected = false;
        while(!connected) {
            try {
                socket = new Socket(host, port);
                connected = true;
            } catch (IOException exception) {
                System.err.printf("[Error] Failed to connect to %s:%d, trying again in 5 seconds...\n", host, port);
            }
        }
        System.out.printf("Connected to %s:%d.\n", host, port);
    }

    public void disconnect() {
        send("{\"message_id\": 0, \"message_type\": \"disconnect_machine\"}");
        close();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException exception) {
            System.err.println("[Error] Failed to Close Socket!");
        }
    }

    public void send(String toSend) {
        try {
            new PrintWriter(socket.getOutputStream()).println(toSend);
        } catch (IOException e) {
            System.err.println("[Error] Failed to Write to Socket!");
            e.printStackTrace();
        }
    }

}
