package machine.transport;

import java.util.HashMap;

public class MessageTypes {

    private static final HashMap<String, Class<? extends Message>> messageTypes = new HashMap<>();

    public static void register(String encodedName, Class<? extends Message> clazz) {
        messageTypes.put(encodedName, clazz);
    }

    public static boolean exists(String encodedName) {
        return messageTypes.containsKey(encodedName);
    }

    public static Class<? extends Message> get(String encodedName) {
        return messageTypes.get(encodedName);
    }

    static {
        register("disconnect_machine", MessageDisconnectMachine.class);
        register("heartbeat", MessageHeartbeat.class);
        register("machine_descriptor", MessageMachineDescriptor.class);
    }

}
