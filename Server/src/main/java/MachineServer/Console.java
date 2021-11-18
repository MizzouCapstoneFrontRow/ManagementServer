package MachineServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class Console extends Thread {
	PrintStream out;
	InputStream in;
	
	private static SimpleDateFormat dateFormat;
	
	private static Console console;
	
	Console() {
		this(System.out, System.in);
	}
	
	Console(PrintStream out, InputStream in) {
		if(in == null) {
			throw new IllegalArgumentException("Console InputStream cannot be null");
		}
		this.out = out;
		this.in = in;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	
	@Override
	public void run() {
		BufferedReader in = new BufferedReader(new InputStreamReader(this.in));
		try {
			do {
				out.print("$ ");
			} while(CommandHandler.execute(in.readLine()) != Command.EXIT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Console startLogging() {
		console = new Console();
		console.start();
		return console;
	}
	
	public static PrintStream out() {
		return console.out;
	}
	
	public static InputStream in() {
		return console.in;
	}
	
	/* Logs the given message to this Console's
	 * PrintStream. Will throw NullPointerException
	 * if the Console has not yet been initialized.
	 */
	public static void log(String message) throws NullPointerException {
		console.out.format("[%s] %s\n$ ", dateFormat.format(Date.from(Instant.now())), message);
	}
}
