package com.github.mizzoucapstonefrontrow.managementserver.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
		Integer toReturn = null;
		try {
			toReturn = Integer.parseInt(get(setting));
		} catch (Throwable t) {
			Console.format("Failed to parse integer \"%s\" from config file \"%s\"", setting, SETTINGS_FILE);
		}
		return toReturn;
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
			Console.format("Failed to save settings file \"%s\"", SETTINGS_FILE);
			e.printStackTrace();
		}
	}
	
	public void load() {
		try {

			// If config missing, attempt to copy default config from classpath to working directory
			File file = new File(SETTINGS_FILE);
			if (!file.exists()) {
				InputStream defaultSettingsFile = (getClass().getResourceAsStream("/" + SETTINGS_FILE));
				Files.copy(defaultSettingsFile, Paths.get(SETTINGS_FILE), StandardCopyOption.REPLACE_EXISTING);
			}

			// Load config from working directory
			FileReader reader = new FileReader(SETTINGS_FILE);
			settings = json.fromJson(reader, new TypeToken<HashMap<String, String>>() {}.getType());
			reader.close();

		} catch (Exception e) {
			Console.format("Failed to load config file \"%s\" - working directory is \"%s\".", SETTINGS_FILE, (new java.io.File(".")).getAbsolutePath());
			e.printStackTrace();
			settings = new HashMap<>();
		}
	}
	
	public static SettingsManager init() {
		if(instance == null) {
			instance = new SettingsManager();
		}
		return instance;
	}
}