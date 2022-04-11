package com.github.mizzoucapstonefrontrow.managementserver.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class Console extends Thread {
	PrintStream out;
	InputStream in;
	
	private SimpleDateFormat dateFormat;
	private Thread thread;
	private static Console console;
	
	
	private Console() {
		this(System.out, System.in);
	}
	
	private Console(PrintStream out, InputStream in) {
		if(in == null) {
			throw new IllegalArgumentException("Console InputStream cannot be null");
		}
		this.out = out;
		this.in = in;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		thread = currentThread();
	}
	/*
	@Override
	public void run() {
		BufferedReader in = new BufferedReader(new InputStreamReader(this.in));
		out.print("$ ");
		try {
			Command c;
			do {
				c = CommandHandler.execute(in.readLine());
				out.print("$ ");
			} while(c != Command.EXIT);
			// join main thread so logging can finish before the logger closes
			Server.thread.join();
		} catch (Exception e) {
			log("Console exited forcefully");
			e.printStackTrace(out);
		} finally {
			out.close();
		}
		console = null;
	} */
	
	public static Console startLogging() {
		console = new Console();
		// required for graceful shutdown
		console.setDaemon(true);
		console.start();
		return console;
	}
	
	public static Console getLogger() {
		return console;
	}
	
	// assumes that this method will only be called on Server exit
	public static Boolean stopLogging() {
		if(console == null) {
			return false;
		}
		try {
			log("Logging stopped");
			console.in.close();
		} catch (IOException e) {
			return false;
		}
		return true;
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
	public static synchronized void log(String message) throws NullPointerException {
		console.out.format("[%s] %s\n$ ", console.dateFormat.format(Date.from(Instant.now())), message);
	}
	
	public static void format(String format, Object... args) {
		log(String.format(format, args));
	}
}
