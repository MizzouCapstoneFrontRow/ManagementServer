package MachineServer;

enum AxisType {
	ROTATIONAL, LINEAR
}

public class Axis {
	String id;
	AxisType type;
	double minValue;
	double maxValue;
}
