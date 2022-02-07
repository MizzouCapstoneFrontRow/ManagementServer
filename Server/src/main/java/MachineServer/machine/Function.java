package MachineServer.machine;
import java.util.LinkedHashMap;

public class Function {
	protected String name;
	protected LinkedHashMap<String, String> parameters;
	protected LinkedHashMap<String, String> returns;
	
	/* Constructs a function with the specified name,
	 * parameter list, and return list
	 */
	public Function(String name, LinkedHashMap<String, String> parameters, LinkedHashMap<String, String> returns) {
		this.name = name;
		this.returns = returns;
		this.parameters = parameters;
	}
	
	/* Constructs a function with the specified name,
	 * and no parameters or return value.
	 */
	public Function(String name) {
		parameters = new LinkedHashMap<String, String>();
		returns = new LinkedHashMap<String, String>();
		this.name = name;
	}
	
	// TODO convert Object array into parameter list
}
