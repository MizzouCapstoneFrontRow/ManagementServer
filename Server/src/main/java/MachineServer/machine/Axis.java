package MachineServer.machine;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class Axis extends Feature {
	protected LinkedHashMap<String, String> range;
	
	public Boolean hasRange(String key) {
		return range.containsKey(key);
	}
	
	public String getRange(String key) {
		return range.get(key);
	}
	
	public List<String> getRanges() {
		LinkedList<String> l = new LinkedList<String>();
		l.addAll(range.keySet());
		return l;
	}
}
