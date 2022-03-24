package machine.descriptor;

import machine.server.Console;
import machine.server.Server;
import machine.transport.Message;
import machine.transport.Messenger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class Machine extends Thread implements Messenger {
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private String displayName;
	private Feature[] features;
	private Function[] functions;
	
	public HashMap<Integer, BiConsumer<Messenger, Message>> listeners;
	
	private Machine() {
		listeners = new HashMap<Integer, BiConsumer<Messenger, Message>>();
	}
	
	private Machine(Socket socket, InputStreamReader in, OutputStreamWriter out) {
		this();
		this.socket = socket;
		this.in = new BufferedReader(in);
		this.out = new BufferedWriter(out);
	}
	
	public static Machine valueOf(Socket socket) throws IOException {
		InputStreamReader in = new InputStreamReader(socket.getInputStream());
		BufferedReader temp = new BufferedReader(in);
		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
		
		Machine m = Server.json.fromJson(temp.readLine(), Machine.class);
		
		m.in = temp;
		m.out = new BufferedWriter(out);
		m.socket = socket;
		
		return m;
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
			return Message.valueOf(read());
		} catch (IOException e) {
			return null;
		}
	}
	
	// Writes a Message object to the socket
	public Boolean writeMessage(Message message) {
		try {
			write(message.toString());
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public void shutdown() throws IOException {
		Server.clients.remove(displayName);
		socket.close();
		Console.format("Machine %s has disconnected", getName());
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
	@Override
	public void run() {		
		Server.clients.put(displayName, this);
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
		} catch (IOException e) {
			Console.log("Error while shutting down machine connection");
			e.printStackTrace();
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
}





























