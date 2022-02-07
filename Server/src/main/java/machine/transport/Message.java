package machine.transport;

import machine.descriptor.Function;
import machine.server.Server;

public class Message {
	public Function function;
	public Object[] args;
	public String content;
	
	public Message(String content, Function f, Object... args) {
		this(f, args);
		this.content = content;
	}
	
	public Message(Function f, Object... args) {
		this.function = f;
		this.args = args;
	}
	
	public Message(String content) {
		this.function = null;
		this.args = null;
		this.content = content;
	}
	
	public static Message valueOf(String json) {
		try {
			return Server.json.fromJson(json, Message.class);
		} catch(Exception e) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return Server.json.toJson(this);
	}
}
