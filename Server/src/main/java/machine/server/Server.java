package machine.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.HashMap;

import machine.descriptor.Machine;
import machine.transport.Message;

import com.google.gson.Gson;

public class Server {
	public static ServerSocket socket;
	public static HashMap<String, Machine> clients;
	public static UnityListener unity;
	public static Gson json;
	public static SettingsManager settings;
	public static Console console;
	
	protected static Thread thread;

	public static void main(String[] args) {
		console = Console.startLogging();
		settings = SettingsManager.init();
		try {
			Console.log("Server starting up");
			socket = new ServerSocket(settings.getInt("Machine-port"));
			clients = new HashMap<String, Machine>();
			unity = new UnityListener();
			json = new Gson();
			thread = Thread.currentThread();
			unity.start();
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
			unity.shutdown();
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
				// machine connection monitoring happens in new thread
				machine.start();
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
