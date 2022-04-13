package com.github.mizzoucapstonefrontrow.managementserver.server;

import com.google.gson.Gson;
import com.github.mizzoucapstonefrontrow.managementserver.thread.Machine;
import com.github.mizzoucapstonefrontrow.managementserver.thread.UserEnvironmentListener;
import com.github.mizzoucapstonefrontrow.managementserver.transport.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Optional;

public class Server {
	public static ServerSocket socket;
	public static HashMap<String, Machine> clients;
	public static UserEnvironmentListener userEnvironment;
	public static Gson json;
	public static SettingsManager settings;
	public static Console console;
	
	protected static Thread thread;

	public static void main(String[] args) {
		console = Console.startLogging();
		settings = SettingsManager.init();
		try {
			Console.log("Server starting up");
			socket = new ServerSocket(Optional.ofNullable(settings.getInt("machine_port")).orElse(45575));
			clients = new HashMap<String, Machine>();
			userEnvironment = new UserEnvironmentListener();
			json = new Gson();
			thread = Thread.currentThread();
			userEnvironment.start();
			loop();
		} catch(SocketException e) {
			Console.log("Socket closed, server exiting");
			e.printStackTrace();
		} catch(Exception e) {
			Console.log("An exception occurred while executing the Main thread:");
			e.printStackTrace(console.out);
		}
		try {
			thread.interrupt();
			socket.close();
			userEnvironment.shutdown();
		} catch (Throwable t) {
			Console.log("Failed to Properly Shutdown!");
		}
		Console.stopLogging();
	}
	
	public static void loop() throws IOException {
		Console.log("Accepting connections");
		while(!socket.isClosed()) {
			try {
				Machine machine = Machine.valueOf(socket.accept());
				if(!clients.containsKey(machine.getName())) {
					machine.start();
					Server.clients.put(machine.getName(), machine);
				}
				else {
					Console.format("Disconnecting Inbound Client: Name \"%s\" is already in use!", machine.getName());
					Message disconnectMessage = new Message("{\"message_type\":\"disconnect\"}");
					machine.writeMessage(disconnectMessage);
					machine.shutdown(); // This cleanup is still necessary, even though the thread is never started.
				}
			} catch (Throwable t) {
				if(!socket.isClosed()) Console.log("Failed to accept socket!");
			}
		}
	}
	
	public Boolean sendMessage(Message message, Machine toMachine) {
		Machine m = clients.get(toMachine.toString());
		if(m != null) {
			m.writeMessage(message);
			return true;
		}
		return false;
	}
}
