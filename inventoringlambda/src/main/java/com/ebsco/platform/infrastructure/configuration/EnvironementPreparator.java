package com.ebsco.platform.infrastructure.configuration;

import com.ebsco.platform.infrastructure.utility.PropertiesUtils;

import java.util.Properties;

public class EnvironementPreparator {

	private Properties properties;

public EnvironementPreparator(Properties properties) {
	this.properties = properties;
}

public void prepare(){

	boolean isIDGiven = PropertiesUtils.setSystemPropertyIfPresent(properties, ConfigConstants.SYS_P_NAME_AWS_ID);
	boolean isKey = PropertiesUtils.setSystemPropertyIfPresent(properties, ConfigConstants.SYS_P_NAME_AWS_KEY);
}

}
