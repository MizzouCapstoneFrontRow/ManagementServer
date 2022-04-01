package machine.transport;

import java.util.function.BiConsumer;

import com.google.gson.JsonObject;

import machine.descriptor.Machine;
import machine.server.Console;
import machine.server.Server;

public enum Command {
	nop(null),
	// ---------------------------------------------------------------- EXIT
	exit((messenger, message) -> {
		// A messenger has requested full system shutdown
		try {
			for(Machine machine : Server.clients.values()) {
				machine.shutdown();
			}
			Server.socket.close();
		} catch(Exception e) {
			Console.log("An exception occurred during server shutdown");
			e.printStackTrace(Console.out());
		}
	// ---------------------------------------------------------------- LIST
	}), list((messenger, message) -> {
		/* We re-use the same Message Object and pass it back to the caller
		 * With the list of clients as the content
		 */
		message.content = Server.json.toJson(Server.clients.keySet());
		messenger.writeMessage(message);
	// ---------------------------------------------------------------- MACHINE_DESCRIPTION
	}), machine_description((messenger, message) -> {
		// This is automatic, so we do nothing
	// ---------------------------------------------------------------- DISCONNECT
	}), disconnect((messenger, message) -> {
		if(messenger instanceof Machine) {
			Server.clients.remove(messenger.toString());
		}
	// ---------------------------------------------------------------- FUNCTION_CALL
	}), function_call((messenger, message) -> {
		// We convert the "content" member of the message into a JsonObject
		JsonObject content = Server.json.fromJson(message.content, JsonObject.class);
		/* The expected structure of the "content" object is:
		 *	{"target": "machine name", "call": {[function call object]}}
		 */
		
		// We use the "target" member of the message to get the correct machine
		Messenger m = Server.clients.get(content.get("target").getAsString());
		
		/* We re-use the same Message Object and pass it onto the client
		 * With the "target" member removed
		 */
		message.content = content.get("call").getAsString();
		m.writeMessage(message);
	// ---------------------------------------------------------------- FUNCTION_RETURN
	}), function_return((messenger, message) -> {
		Console.format("Function return from: %s", messenger);
		// When a function return is received, return it immediately to Unity
		Server.unity.writeMessage(message);
	});
	
	// ---------------------------------------------------------------- MECHANICAL STUFF
	
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

























