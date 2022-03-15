package machine.server;

import java.util.function.BiConsumer;

import machine.descriptor.Machine;
import machine.transport.Message;
import machine.transport.Messenger;

public class CommandHandler {
	
	/* Executes console command input and returns the
	 * Command value associated with the given input,
	 * or Command.NOP if no matching command was found.
	 */
	/*
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

		return Command.EXIT;
	}
	*/
}
