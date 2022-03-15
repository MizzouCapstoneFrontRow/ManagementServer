package machine.transport;

import java.util.function.BiConsumer;

import machine.descriptor.Machine;
import machine.server.Console;
import machine.server.Server;

public enum Command {
	EXIT((messenger, message) -> {
		try {
			for(Machine machine : Server.clients.values()) {
				machine.shutdown();
			}
			Server.socket.close();
		} catch(Exception e) {
			Console.log("An exception occurred during server shutdown");
			e.printStackTrace(Console.out());
		}
	}), LIST((messenger, message) -> {
		
	}), NOP(null);
	
	private BiConsumer<Messenger, Message> action;
	
	Command(BiConsumer<Messenger, Message> action) {
		this.action = action;
	}
	
	void invoke(Messenger messenger, Message message) {
		if(action != null) {
			action.accept(messenger, message);
		}
	}
}