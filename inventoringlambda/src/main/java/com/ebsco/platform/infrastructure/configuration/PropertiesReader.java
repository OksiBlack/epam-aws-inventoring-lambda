package com.ebsco.platform.infrastructure.configuration;

import com.ebsco.platform.utility.PropertiesUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertiesReader {
private Path propPath;
private Properties properties;

public PropertiesReader(String path) throws IOException {
	this(Paths.get(path));

}

public Properties getProperties() {
	return properties;
}

public PropertiesReader(Path path) throws IOException {
	this.propPath = path;
}

public PropertiesReader() throws IOException {
	this(ConfigConstants.CONFIG_FILE_NAME);
}

public void init() throws IOException {
	Path path = propPath.toRealPath();

	properties = new Properties();
	properties.load(Files.newBufferedReader(path));

	boolean isIDGiven = PropertiesUtils.setSystemPropertyIfPresent(properties, ConfigConstants.SYS_P_NAME_AWS_ID);

	boolean isKey = PropertiesUtils.setSystemPropertyIfPresent(properties, ConfigConstants.SYS_P_NAME_AWS_KEY);

}


}

