/**
 * 
 */
package com.trei.cerberus.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @date 06 Dec 2013
 * @author Kovalskiy Andrey
 */
public class PropertiesManager {

	private static Properties properties;
	
	/**
	 * Block initializes properties    
	 */
	static {
		properties = new Properties();
		try {
			properties.load(new FileInputStream("./resources/properties.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * It is not possible to initialize this class
	 */
	private PropertiesManager() {}

	/**
	 * Is used to get a property value as a String by key.
	 * 
	 * @param key the property key.
	 * @return the value with the specified key value.
	 */
	public static String acqureProperty(String key) {
		return properties.getProperty(key);
	}

	/**
	 * Is used to get a property value as a String by key. Returns the default
	 * value argument if the property is not found.
	 * 
	 * @param key the property key.
	 * @return the value with the specified key value.
	 */
	public static String acqureProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

}
