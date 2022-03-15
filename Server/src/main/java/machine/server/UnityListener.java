package machine.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import machine.transport.Message;
import machine.transport.Messenger;

public class UnityListener extends Thread implements Messenger {
	ServerSocket socket;
	Socket connection;
	BufferedReader in;
	BufferedWriter out;
	
	public UnityListener() throws IOException {
		socket = new ServerSocket(Server.settings.getInt("Unity-port"));
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
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
}
