package machine.transport;

import machine.descriptor.Machine;
import machine.server.Console;
import machine.server.Server;

public class Message {

	// Using snake case to match JSON encoding
	public Integer message_id;
	public Command command;
	
	
	public static Message valueOf(String json) {
		try {
			return Server.json.fromJson(json, Message.class);
		} catch(Exception e) {
			Console.log("Failed to Read Message!");
			e.printStackTrace();
			return null;
		}
		
		/*
		try {
			Message baseMessage = Server.json.fromJson(json, Message.class);
			if(baseMessage != null && baseMessage.message_type != null) {
				return Server.json.fromJson(json, MessageTypes.get(baseMessage.message_type));
			}
			return baseMessage;
		} catch(Exception e) {

		} */
	}

	public boolean shouldForwardToUserEnvironments() {
		return false;
	}

	/**
	 * Performs local handling prior to passthrough, if applicable.
	 */
	public void handle(Machine connection) {}
	
	@Override
	public String toString() {
		return Server.json.toJson(this);
	}

}
