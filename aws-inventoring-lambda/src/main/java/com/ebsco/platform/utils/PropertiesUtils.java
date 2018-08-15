package com.ebsco.platform.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public class PropertiesUtils {
	public static final Logger logger = LogManager.getLogger();
/**
 * @param props
 * @param propertyName
 * @return
 */
public static boolean setSystemPropertyIfPresent(Properties props, String propertyName) {
	boolean isChanged = false;
	Objects.requireNonNull(props);
	String property = props.getProperty(propertyName);

	if (property != null) {
		System.setProperty(propertyName, property);
		isChanged = true;
		logger.debug("{} set from properties.\n", propertyName);

		assert System.getProperty(propertyName)
				.equals(property) : "System property not set.";
	}


	return isChanged;
}


/**
 * @throws IOException
 */
public static Properties loadProperties(Path path) throws IOException {
	Path realPath = path.toRealPath();

	Properties properties = new Properties();
	properties.load(Files.newBufferedReader(realPath));


	return properties;
}
}






