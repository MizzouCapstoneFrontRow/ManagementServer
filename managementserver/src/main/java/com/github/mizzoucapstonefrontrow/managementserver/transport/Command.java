package com.github.mizzoucapstonefrontrow.managementserver.transport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.github.mizzoucapstonefrontrow.managementserver.server.Console;
import com.github.mizzoucapstonefrontrow.managementserver.server.Server;
import com.github.mizzoucapstonefrontrow.managementserver.thread.Machine;

import java.io.IOException;
import java.util.Collection;
import java.util.function.BiConsumer;

public enum Command {
	// ---------------------------------------------------------------- NOP
	nop((messenger, message) -> {}),
	// ---------------------------------------------------------------- EXIT
	exit((messenger, message) -> {
		// A messenger has requested full system shutdown
		try {
			Collection<Machine> clients = Server.clients.values();
			for(Machine machine : clients) {
				machine.shutdown();
				Server.clients.remove(machine.getName());
			}
			Server.socket.close();
		} catch(Exception e) {
			Console.log("An exception occurred during server shutdown");
			e.printStackTrace(Console.out());
		}
	}),
	// ---------------------------------------------------------------- LIST
	list((messenger, message) -> {
		/* We re-use the same Message Object and pass it back to the caller
		 * With the list of clients as the content
		 */
		JsonArray devices = Server.json.fromJson(Server.json.toJson(Server.clients.keySet()), JsonArray.class);
		message.content = new JsonObject();
		message.content.add("Devices", devices);
		messenger.writeMessage(message);
	}),
	// ---------------------------------------------------------------- SELECT
	select((messenger, message) -> {
		// Message from Unity requesting the machine descriptor given the target's name
		JsonObject messageContent = Server.json.fromJson(message.content, JsonObject.class);
		Machine targetMachine = Server.clients.get(messageContent.get("target").getAsString());
		Server.unity.writeMessage(new Message(targetMachine.getMachineDescriptor()));
	}),
	// ---------------------------------------------------------------- MACHINE_DESCRIPTION
	machine_description((messenger, message) -> {
		// This is automatic, so we do nothing
	}),
	// ---------------------------------------------------------------- RESET
	reset((messenger, message) -> {
		// NOP - this should only ever be sent from the server, never received.
	}),
	// ---------------------------------------------------------------- DISCONNECT
	disconnect((messenger, message) -> {
		if(messenger instanceof Machine) {
			Message resetMessage = new Message("{\"message_type\":\"reset\",\"target\":\"" + messenger.toString() + "\"}");
			Server.unity.writeMessage(resetMessage);
			try {
				messenger.shutdown();
			} catch (IOException ignored) {}
			Server.clients.remove(messenger.toString());
		}
		else {
			Message resetMessage = new Message("{\"message_type\":\"reset\"}");
			for(Machine client : Server.clients.values()) {
				client.writeMessage(resetMessage);
			}
		}
	}),
	// ---------------------------------------------------------------- FUNCTION_CALL
	function_call((messenger, message) -> {
		// We convert the "content" member of the message into a JsonObject
		JsonObject content = Server.json.fromJson(message.content, JsonObject.class);
		/* The expected structure of the "content" object is:
		 *	{"target": "machine name", "call": {[function call object]}}
		 */

		// We use the "target" member of the message to get the correct machine
		Messenger m = Server.clients.get(message.content.get("target").getAsString());

		/* We re-use the same Message Object and pass it onto the client
		 * With the "target" member removed
		 */
		message.content = message.content.get("call").getAsJsonObject();
		m.writeMessage(message);
	}),
	// ---------------------------------------------------------------- FUNCTION_RETURN
	function_return((messenger, message) -> {
		Console.format("Function return from: %s", messenger);
		// When a function return is received, return it immediately to Unity
		Server.unity.writeMessage(message);
	}),
	// ---------------------------------------------------------------- SENSOR_READ
	sensor_read((userMessenger, message) -> {
		// Message from Unity representing a request to read a sensor.
		JsonObject messageContent = Server.json.fromJson(message.content, JsonObject.class);
		Messenger clientMessenger = Server.clients.get(messageContent.get("target").getAsString());
		clientMessenger.writeMessage(message);
	// ---------------------------------------------------------------- SENSOR_RETURN
	}),
	sensor_return((clientMessenger, message) -> {
		// Message from a Client representing a reply to a sensor read with the value.
		Server.unity.writeMessage(message);
	}),
	// ---------------------------------------------------------------- AXIS_CHANGE
	axis_change((userMessenger, message) -> {
		// Message from Unity representing a request to change an axis.
		JsonObject messageContent = Server.json.fromJson(message.content, JsonObject.class);
		Messenger clientMessenger = Server.clients.get(messageContent.get("target").getAsString());
		clientMessenger.writeMessage(message);
	}),
	// ---------------------------------------------------------------- AXIS_RETURN
	axis_return((clientMessenger, message) -> {
		// Message from a Client representing a reply to an axis change.
		Server.unity.writeMessage(message);
	}),
	// ---------------------------------------------------------------- UNSUPPORTED_OPERATION
	unsupported_operation((inboundMessenger, message) -> {

		JsonObject messageContent = Server.json.fromJson(message.content, JsonObject.class);
		JsonElement messageTarget = messageContent.get("target");
		try {
			Console.format("Caught Unsupported Operation! Operation: \"%s\", Reason:\"%s\".", messageContent.get("operation").getAsString(), messageContent.get("reason").getAsString());
		} catch (Throwable ignored) {}

		if(inboundMessenger instanceof Machine) {
			Server.unity.writeMessage(message);
		}
		else if(messageTarget != null) {
			Messenger clientMessenger = Server.clients.get(messageTarget.getAsString());
			clientMessenger.writeMessage(message);
		}

	});

	private final BiConsumer<Messenger, Message> action;

	Command(BiConsumer<Messenger, Message> action) {
		this.action = action;
	}

	void invoke(Messenger messenger, Message message) {
		if(action != null) {
			action.accept(messenger, message);
		}
	}
}

























