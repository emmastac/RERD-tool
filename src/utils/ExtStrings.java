package utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtStrings {
	private static final String BUNDLE_NAME = "utils.strings"; 

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	private static final Logger logger = Logger.getLogger(ExtStrings.class.getName());

	private ExtStrings() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			logger.log(Level.SEVERE, e.toString(), e);
			return '!' + key + '!';
		}
	}
	
	public static Double getInt(String key) {
		try {
			return Double.valueOf(RESOURCE_BUNDLE.getString(key));
		} catch (MissingResourceException | NumberFormatException e) {
			logger.log(Level.SEVERE, e.toString(), e);
			return 0.0;
		} 
	}
}
