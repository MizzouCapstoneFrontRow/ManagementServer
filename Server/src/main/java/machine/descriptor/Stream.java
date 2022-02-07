package machine.descriptor;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class Stream extends Feature {
	protected int port;
	LinkedHashMap<String, String> characteristics;
	
	public int getPort() {
		return port;
	}
	
	public Boolean hasCharacteristic(String key) {
		return characteristics.containsKey(key);
	}
	
	public String getCharacteristic(String key) {
		return characteristics.get(key);
	}
	
	public List<String> getCharacteristics() {
		LinkedList<String> l = new LinkedList<String>();
		l.addAll(characteristics.keySet());
		// ensures that the map is not modified externally
		return l;
	}
}
