package machine.descriptor;
import machine.server.Console;
import machine.server.Server;
import machine.transport.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Machine extends Thread {
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private String displayName;
	private Feature[] features;
	private Function[] functions;
	
	private Machine(Socket socket, InputStreamReader in, OutputStreamWriter out) {
		this.socket = socket;
		this.in = new BufferedReader(in);
		this.out = new BufferedWriter(out);
	}
	
	public static Machine valueOf(Socket socket) throws IOException {
		InputStreamReader in = new InputStreamReader(socket.getInputStream());
		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
		
		return new Machine(socket, in, out);
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

	public void shutdown() throws IOException {
		Server.clients.remove(displayName);
		socket.close();
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
	@Override
	public void run() {
		Console.log("Instantiating Machine from Inbound Connection on Port #" + socket.getPort());
		// TODO validation - assign machine ID and retrieve description from client
		while(!socket.isClosed()) {
			Message m = readMessage();
			if(m != null) {
				m.handle(this);
				if (m.shouldForwardToUserEnvironments()) {
					// TODO message passthrough
				}
			}
		}
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Console.log("Terminating Machine Instance from Closed Connection on Port #" + socket.getPort());
	}
}
