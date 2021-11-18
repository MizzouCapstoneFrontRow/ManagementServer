package MachineServer;
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
	private String ID;
	private Axis[] axes;
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
		try {
			return Message.valueOf(read());
		} catch(Exception e) {
			e.printStackTrace();
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
	
	protected void shutdown() throws IOException {
		Server.clients.remove(ID);
		socket.close();
	}
	
	@Override
	public String toString() {
		return ID;
	}
	
	@Override
	public void run() {
		// TODO validation - assign machine ID and retrieve description from client
		while(!socket.isClosed()) {
			Message m = readMessage();
			// TODO command handling
		}
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
