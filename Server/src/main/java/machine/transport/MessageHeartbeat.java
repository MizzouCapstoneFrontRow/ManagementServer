package machine.transport;

import machine.descriptor.Machine;

public class MessageHeartbeat extends Message {

    public Boolean shouldReply = false;

    @Override
    public void handle(Machine connection) {
        if(shouldReply) {
            connection.writeMessage(new MessageHeartbeat());
        }
    }

}
