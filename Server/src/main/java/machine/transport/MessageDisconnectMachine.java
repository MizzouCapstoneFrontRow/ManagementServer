package machine.transport;

import machine.descriptor.Machine;
import machine.server.Console;

public class MessageDisconnectMachine extends Message {

    @Override
    public boolean shouldForwardToUserEnvironments() {
        return true;
    }

    @Override
    public void handle(Machine connection) {
        try {
            connection.shutdown();
        } catch (Exception e) {
            Console.log("An exception occurred during machine disconnect");
            e.printStackTrace();
        }
    }

}
