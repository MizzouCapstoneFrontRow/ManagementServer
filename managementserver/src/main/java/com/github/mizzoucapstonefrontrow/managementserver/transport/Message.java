package com.github.mizzoucapstonefrontrow.managementserver.transport;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.github.mizzoucapstonefrontrow.managementserver.server.Console;
import com.github.mizzoucapstonefrontrow.managementserver.server.Server;

public class Message {
	public static Integer generated_id = 0;

	// Using snake case to match JSON encoding
	public Integer message_id;
	public Command command;
	public JsonObject content;
	
	Message(Integer message_id, Command command) {
		this.message_id = message_id;
		this.command = command;
	}
	
	Message(Integer message_id, Command command, JsonObject content) {
		this(message_id, command);
		this.content = content;
	}

	Message(Integer message_id, Command command, String content) {
		this(message_id, command);
		JsonObject jsonObject = null;
		try {
			jsonObject = Server.json.fromJson(content, JsonObject.class);
		} catch (JsonSyntaxException jsonSyntaxException) {
			Console.format("Caught JsonSyntaxException when Constructing Message!\nRaw Message: \"%s\"", content);
			jsonSyntaxException.printStackTrace(Console.out());
		}
		this.content = jsonObject;
	}

	public Message(JsonObject content) {
		this(generated_id++, null, content);
	}
	
	public Message(String content) {
		this(generated_id++, null);
		JsonObject jsonObject = null;
		try {
			jsonObject = Server.json.fromJson(content, JsonObject.class);
			jsonObject.addProperty("message_id", message_id);
		} catch (JsonSyntaxException jsonSyntaxException) {
			Console.format("Caught JsonSyntaxException when Constructing Message!\nRaw Message: \"%s\"", content);
			jsonSyntaxException.printStackTrace(Console.out());
		}
		this.content = jsonObject;
	}
	
	public static Message valueOf(String json) {
		try {
			JsonObject content = Server.json.fromJson(json, JsonObject.class);
			int message_id = -1;
			try { message_id = content.get("message_id").getAsInt(); } catch (Exception ignored) {}
			Message toReturn = new Message(message_id, Command.valueOf(content.get("message_type").getAsString()), content);
			//Console.format("--- MESSAGE ---\nmessage_id: %s\n\n\ncommand:\n%s\n\n\ncontent:\n%s\n--- END MESSAGE ---", toReturn.message_id, toReturn.command, toReturn.content);
			return toReturn;
		} catch (Throwable t) {
			Console.format("Failed to Read Message! Caught Exception \"%s\".\nRaw Message: \"%s\"\n", t.toString(), json == null ? "null" : json);
			//t.printStackTrace(Console.out());
		}
		return null;
	}
	
	public void invoke(Messenger messenger) {
		command.invoke(messenger, this);
	}
	
	@Override
	public String toString() {
		JsonObject contentToSend = content.deepCopy();
		contentToSend.addProperty("message_id", message_id);
		String toReturn = Server.json.toJson(contentToSend);
		//Console.format("Message::toString - \"%s\"", toReturn);
		return toReturn;
	}

}
