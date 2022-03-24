package machine.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.BiConsumer;

import machine.transport.Message;
import machine.transport.Messenger;

@SuppressWarnings("unused")
public class UnityListener extends Thread implements Messenger {
	ServerSocket socket;
	Socket connection;
	BufferedReader in;
	BufferedWriter out;
	
	public HashMap<Integer, BiConsumer<Messenger, Message>> listeners;
	
	public UnityListener() throws IOException {
		socket = new ServerSocket(Server.settings.getInt("Unity-port"));
		listeners = new HashMap<Integer, BiConsumer<Messenger, Message>>();
	}
	
	@Override
	public void run() {
		while(!socket.isClosed()) {
			try {
				connection = socket.accept();
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
				
				while(isReady()) {
					Message m = readMessage();
					if(m == null) {
						break;
					} else if(!react(m)) {
						m.invoke(this);
					}
				}
			} catch (IOException e) {
				Console.log("Unity connection closed");
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
		Message toReturn;
		try {
			toReturn = Message.valueOf(read());
		} catch(Exception e) {
			e.printStackTrace();
			toReturn = null;
		}
		if(toReturn == null) {
			//Console.log("Failed to Read Message!");
		}
		return toReturn;
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

	@Override
	public Boolean isReady() {
		return connection != null && !connection.isClosed();
	}

	@Override
	public void shutdown() throws IOException {
		socket.close();
		connection.close();
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





























