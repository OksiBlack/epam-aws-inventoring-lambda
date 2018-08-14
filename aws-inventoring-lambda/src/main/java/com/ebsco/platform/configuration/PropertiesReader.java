package com.ebsco.platform.configuration;

import com.ebsco.platform.utils.FileUtils;
import com.ebsco.platform.utils.PropertiesUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOError;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


public class PropertiesReader {
private static PropertiesReader reader = new PropertiesReader();

private Properties properties;
public static final Logger logger = LogManager.getLogger();


private PropertiesReader() {
	String property = System.getProperty(ConfigConstants.SYS_P_NAME_CONFIG_FILE_NAME,
			ConfigConstants.DEFAULT_CONFIG_FILE_NAME);
	try {

		Path propPath = FileUtils.findFirstDeeperInDirByName(Paths.get("."), property);
		properties = PropertiesUtils.loadProperties(propPath);

		boolean isIDGiven = PropertiesUtils.setSystemPropertyIfPresent(properties, ConfigConstants.SYS_P_NAME_AWS_ID);
		boolean isKey = PropertiesUtils.setSystemPropertyIfPresent(properties, ConfigConstants.SYS_P_NAME_AWS_KEY);
	}
	catch (IOException e) {
		throw new IOError(e);
	}

}

/**
 * @return
 */
public static PropertiesReader getInstance() {
	return reader;
}

/**
 * \
 *
 * @return
 */
public Properties getProperties() {
	return properties;
}

/**
 * @param property
 * @return
 */
public String getProperty(String property) {
	return properties.getProperty(property);
}

/**
 * @param property
 * @param defaultV
 * @return
 */
public String getProperty(String property, String defaultV) {
	return properties.getProperty(property, defaultV);
}


/**
 *
 * @param property
 * @param sysPName
 * @param defaultV
 * @return
 */
public String getProperty(String property, String sysPName, String defaultV) {
	String prop = properties.getProperty(property);
	if(prop==null){
		prop=System.getProperty(sysPName);
	}
	return prop==null? defaultV:prop;
}

/**
 * @param propertyName
 * @param val
 */
public void setProperty(String propertyName, String val) {
	properties.setProperty(propertyName, val);
}

/**
 * @param properties
 */
public void setProperties(Properties properties) {
	this.properties = properties;
}

/**
 * @param path
 * @throws IOException
 */
public void setProperties(Path path) throws IOException {
	this.properties = PropertiesUtils.loadProperties(path);
}


}

