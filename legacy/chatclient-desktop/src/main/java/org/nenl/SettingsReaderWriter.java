package org.nenl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsReaderWriter {
	
	private static Logger logger = LoggerFactory.getLogger(SettingsReaderWriter.class);
	
	protected File file;
	protected Map<String, Object> settings;
	
	SettingsReaderWriter() {
		file = new File("settings.json");
		settings = new HashMap<>();
	}
	
	void getSettings() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			String settingsString = "";
			
			while(reader.ready()) {
				settingsString += reader.readLine();
			}
			
			JSONObject settingsJSON = new JSONObject(settingsString);
			
			settings = settingsJSON.toMap();
			
			reader.close();
		} catch (FileNotFoundException e) {
			logger.info("Settings do not exist");
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	void setOneSetting(String key, String value) {
		settings.put(key, value);
		
		saveSettings();
	}
	
	String getOneSetting(String key) {
		return (String)settings.get(key);
	}
	
	boolean settingExist(String key) {
		return settings.containsKey(key);
	}
	
	protected void saveSettings() {
		try {
			FileWriter writer = new FileWriter(file);
			JSONObject settingsJSON = new JSONObject(settings);
			
			writer.write(settingsJSON.toString());
			writer.flush();
			writer.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
