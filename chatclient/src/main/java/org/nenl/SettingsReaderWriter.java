package org.nenl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsReaderWriter {
	
	private static Logger logger = LoggerFactory.getLogger(SettingsReaderWriter.class);
	
	protected File file;
	protected JSONObject settings;
	
	SettingsReaderWriter() {
		file = new File("settings.json");
		settings = new JSONObject();
	}
	
	void getSettings() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			String settingsAsString = "";
			
			while(reader.ready()) {
				settingsAsString += reader.readLine();
			}
			
			settings = new JSONObject(settingsAsString);
			
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
		return settings.getString(key);
	}
	
	boolean settingExist(String key) {
		return settings.has(key);
	}
	
	protected void saveSettings() {
		try {
			FileWriter writer = new FileWriter(file);
			
			writer.write(settings.toString());
			
			writer.flush();
			writer.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
