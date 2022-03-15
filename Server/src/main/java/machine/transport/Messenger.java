package machine.transport;

public interface Messenger {
	public Boolean writeMessage(Message message);
	
	public Message readMessage();
	
	public Boolean isReady();
}
