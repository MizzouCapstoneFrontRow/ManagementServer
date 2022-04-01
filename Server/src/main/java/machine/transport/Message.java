package machine.transport;

import machine.server.Console;
import machine.server.Server;

public class Message {
	public static Integer generated_id = 0;

	// Using snake case to match JSON encoding
	public Integer message_id;
	public Command command;
	public String content;
	
	Message(Integer id, Command c) {
		message_id = id;
		command = c;
	}
	
	Message(Integer id, Command c, String i) {
		this(id, c);
		content = i;
	}
	
	public Message(String content) {
		this(generated_id++, null, content);
	}
	
	public static Message valueOf(String json) {
		try {
			Console.log(json);
			return Server.json.fromJson(json, Message.class);
		} catch(Exception e) {
			Console.log("Failed to Read Message!");
			e.printStackTrace();
			return null;
		}
	}

	public boolean shouldForwardToUserEnvironments() {
		return false;
	}
	
	public void invoke(Messenger messenger) {
		command.invoke(messenger, this);
	}
	
	@Override
	public String toString() {
		return Server.json.toJson(this);
	}

}
