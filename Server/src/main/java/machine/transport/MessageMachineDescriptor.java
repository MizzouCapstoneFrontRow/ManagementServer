package machine.transport;

import machine.descriptor.*;

import java.util.Arrays;

public class MessageMachineDescriptor extends Message {

    public Function[] functions;
    public Sensor[] sensors;
    public Axis[] axes;
    public Stream[] streams;

    @Override
    public void handle(Machine connection) {
        System.out.println("Handling Machine Descriptor Packet!");
        System.out.println("Functions:");
        System.out.println(Arrays.toString(functions));
        System.out.println("Sensors:");
        System.out.println(Arrays.toString(sensors));
        System.out.println("Axes:");
        System.out.println(Arrays.toString(axes));
        System.out.println("Streams:");
        System.out.println(Arrays.toString(streams));
    }

}
