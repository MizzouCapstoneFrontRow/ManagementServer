package com.github.mizzoucapstonefrontrow.managementserver.thread;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.github.mizzoucapstonefrontrow.managementserver.server.Console;
import com.github.mizzoucapstonefrontrow.managementserver.server.Server;
import com.github.mizzoucapstonefrontrow.managementserver.transport.Message;
import com.github.mizzoucapstonefrontrow.managementserver.transport.Messenger;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class Machine extends Thread implements Messenger {

	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private JsonObject description;
	private long lastHeartbeatTime;
	
	public HashMap<Integer, BiConsumer<Messenger, Message>> listeners;
	
	private Machine() {
		listeners = new HashMap<Integer, BiConsumer<Messenger, Message>>();
	}
	
	private Machine(Socket socket, InputStreamReader in, OutputStreamWriter out) throws JsonSyntaxException, IOException {
		this();
		this.socket = socket;
		this.in = new BufferedReader(in);
		this.out = new BufferedWriter(out);
		this.description = Server.json.fromJson(this.in.readLine(), JsonObject.class);
	}
	
	private Machine(Socket socket, BufferedReader in, BufferedWriter out, JsonObject description) {
		this();
		this.socket = socket;
		this.in = in;
		this.out = out;
		this.description = description;
	}
	
	public static Machine valueOf(Socket socket) throws IOException {
		InputStreamReader in = new InputStreamReader(socket.getInputStream());
		BufferedReader bin = new BufferedReader(in);
		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
		BufferedWriter bout = new BufferedWriter(out);
		
		JsonObject m = Server.json.fromJson(bin.readLine(), JsonObject.class);
		
		return new Machine(socket, bin, bout, m);
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
	
	private Boolean react(Message m) {
		if(listeners.containsKey(m.message_id)) {
			var actor = listeners.remove(m.message_id);
			actor.accept(this, m);
			return true;
		}
		return false;
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
			message.content.remove("target");
			write(message.toString());
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public void shutdown() throws IOException {
		socket.close();
		Console.format("Machine %s has disconnected", getName());
	}
	
	@Override
	public String toString() {
		return "Machine \"" + description.get("name").getAsString() + "\"";
	}
	
	public String getID() {
		return toString();
	}
	
	@Override
	public void run() {

		Console.format("Machine %s has connected", getName());
		
		while(isReady()) {
			Message m = readMessage();
			if(m == null) {
				break;
			} else if(!react(m)) {
				m.invoke(this);
			}
		}

		try {
			shutdown();
			Server.clients.remove(getName());
		} catch (IOException e) {
			Console.log("Error while shutting down machine connection");
			e.printStackTrace(Console.out());
		}

	}

	@Override
	public Boolean isReady() {
		return !socket.isClosed();
	}

	@Override
	public void onMessage(Integer messageID, BiConsumer<Messenger, Message> react) {
		listeners.put(messageID, react);
	}

	public JsonObject getMachineDescriptor() {
		return description;
	}

	@Override
	public long getLastHeartbeatTime() {
		return System.currentTimeMillis() - lastHeartbeatTime;
	}

	@Override
	public void setHeartbeat() {
		lastHeartbeatTime = System.currentTimeMillis();
	}

}





























