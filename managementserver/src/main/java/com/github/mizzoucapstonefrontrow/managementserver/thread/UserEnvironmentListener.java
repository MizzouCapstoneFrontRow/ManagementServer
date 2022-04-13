package com.github.mizzoucapstonefrontrow.managementserver.thread;

import com.github.mizzoucapstonefrontrow.managementserver.server.Console;
import com.github.mizzoucapstonefrontrow.managementserver.server.Server;
import com.github.mizzoucapstonefrontrow.managementserver.transport.Message;
import com.github.mizzoucapstonefrontrow.managementserver.transport.Messenger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class UserEnvironmentListener extends Thread implements Messenger {
	ServerSocket socket;
	Socket connection;
	BufferedReader in;
	BufferedWriter out;
	
	public HashMap<Integer, BiConsumer<Messenger, Message>> listeners;
	
	public UserEnvironmentListener() throws IOException {
		socket = new ServerSocket(Optional.ofNullable(Server.settings.getInt("user_environment_port")).orElse(45576));
		listeners = new HashMap<Integer, BiConsumer<Messenger, Message>>();
	}
	
	@Override
	public void run() {
		while(!socket.isClosed()) {
			try {
				connection = socket.accept();
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
				Console.format("Received User Environment Connection from %s:%s", socket.getInetAddress().toString(), socket.getLocalPort());
				
				while(isReady()) {
					Message m = readMessage();
					if(m == null) {
						break;
					} else if(!react(m)) {
						m.invoke(this);
					}
				}
			} catch (IOException e) {
				Console.log("Closed User Environment Connection");
			}
		}
	}
	
	// Reads a line of input from the socket
	private String read() throws IOException {
		return in.readLine();
	}
	
	// Writes a line of output to the socket
	private void write(String message) throws IOException {
		out.write(message);
		out.write('\n');
		out.flush();
	}
	
	// Reads a Message object from the socket
	public Message readMessage() {
		try {
			String stringFromSocket = read();
			return stringFromSocket == null ? null : Message.valueOf(stringFromSocket);
		} catch (IOException e) {
			return null;
		}
	}
	
	// Writes a Message object to the socket
	public Boolean writeMessage(Message message) {
		try {
			write(message.toString());
			return true;
		} catch (Exception e) {
			Console.format("Failed to Write Message!\nRaw Content: \"%s\"", message != null ? (message.content != null ? message.content : "null") : "null");
			e.printStackTrace(Console.out());
			return false;
		}
	}

	@Override
	public Boolean isReady() {
		return connection != null && !connection.isClosed();
	}

	@Override
	public void shutdown() throws IOException {
		socket.close();
		if(connection != null) connection.close();
	}
	
	@Override
	public String toString() {
		return "User Environment";
	}
	
	public String getID() {
		return toString();
	}
	
	private Boolean react(Message m) {
		if(listeners.containsKey(m.message_id)) {
			var actor = listeners.remove(m.message_id);
			actor.accept(this, m);
			return true;
		}
		return false;
	}

	@Override
	public void onMessage(Integer messageID, BiConsumer<Messenger, Message> react) {
		listeners.put(messageID, react);
	}
}





























