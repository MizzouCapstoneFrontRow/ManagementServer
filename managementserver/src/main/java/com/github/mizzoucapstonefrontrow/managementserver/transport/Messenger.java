package com.github.mizzoucapstonefrontrow.managementserver.transport;

import java.io.IOException;
import java.util.function.BiConsumer;

public interface Messenger {
	public Boolean writeMessage(Message message);
	
	public Message readMessage();
	
	public Boolean isReady();
	
	public String getID();
	
	public void onMessage(Integer messageID, BiConsumer<Messenger, Message> react);

	public long getLastHeartbeatTime();

	public void setHeartbeat();
	
	public void shutdown() throws IOException;
}
