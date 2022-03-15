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
	public static HashMap<String, Message> queue;
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
			queue = new HashMap<String, Message>();
			json = new Gson();
			thread = Thread.currentThread();
			loop();
		} catch(SocketException e) {
			Console.log("Socket closed, server exiting");
		} catch(Exception e) {
			Console.log("An exception occurred while executing the Main thread:");
			e.printStackTrace(console.out);
		}
		Console.stopLogging();
	}
	
	public static void loop() throws IOException {
		Console.log("Accepting connections");
		while(!Thread.interrupted()) {
			Machine machine = Machine.valueOf(socket.accept());
			
			// machine connection monitoring happens in new thread
			machine.start();
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
