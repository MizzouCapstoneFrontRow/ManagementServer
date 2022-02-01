package MachineServer;

import MachineServer.machine.Machine;

enum Command {
	EXIT, LIST, NOP;
}

public class CommandHandler {
	
	/* Executes console command input and returns the
	 * Command value associated with the given input,
	 * or Command.NOP if no matching command was found.
	 */
	public static Command execute(String input) {
		String[] args = input.split(" ");
		if(args.length == 0) {
			return Command.NOP;
		}
		switch(Command.valueOf(args[0].toUpperCase())) {
		case EXIT:
			return exit();
		default:
			break;
		}
		return Command.NOP;
	}
	
	private static Command exit() {
		try {
			for(Machine machine : Server.clients.values()) {
				machine.shutdown();
			}
//			Server.thread.interrupt();
			Server.socket.close();
		} catch(Exception e) {
			Console.log("An exception occurred during server shutdown");
			e.printStackTrace(Console.out());
		}
		return Command.EXIT;
	}
}
