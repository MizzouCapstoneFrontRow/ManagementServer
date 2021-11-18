package MachineServer;
import java.util.LinkedHashMap;

public class Function {
	String name;
	LinkedHashMap<String, Class<?>> parameters;
	LinkedHashMap<String, Class<?>> returns;
	
	/* Constructs a function with the specified name,
	 * parameter list, and return list
	 */
	public Function(String name, LinkedHashMap<String, Class<?>> parameters, LinkedHashMap<String, Class<?>> returns) {
		this.name = name;
		this.returns = returns;
		this.parameters = parameters;
	}
	
	/* Constructs a function with the specified name,
	 * and no parameters or return value.
	 */
	public Function(String name) {
		parameters = new LinkedHashMap<String, Class<?>>();
		returns = new LinkedHashMap<String, Class<?>>();
		this.name = name;
	}
	
	// TODO convert Object array into parameter list
}
