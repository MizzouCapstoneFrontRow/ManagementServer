package machine.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class SettingsManager {
	private static final String SETTINGS_FILE = "config.json";
	private static HashMap<String, String> settings;
	private static SettingsManager instance;
	private static Gson json;
	
	private SettingsManager() {
		settings = new HashMap<String, String>();
		json = new GsonBuilder().setPrettyPrinting().create();
		load();
	}
	
	public String get(String setting) {
		return settings.get(setting);
	}
	
	public Integer getInt(String setting) {
		return Integer.parseInt(get(setting));
	}
	
	public String set(String setting, String value) {
		return settings.put(setting, value);
	}
	
	public void write() {
		try {
			FileWriter writer = new FileWriter(SETTINGS_FILE);
			writer.write(json.toJson(settings));
			writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void load() {
		try {
			FileReader reader = new FileReader(SETTINGS_FILE);
			settings = json.fromJson(reader, new TypeToken<HashMap<String, String>>() {}.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static SettingsManager init() {
		if(instance == null) {
			instance = new SettingsManager();
		}
		return instance;
	}
}