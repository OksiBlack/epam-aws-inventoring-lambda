package com.ebsco.platform.utility;
import java.util.Objects;
import java.util.Properties;

public class PropertiesUtils {
/**
 *
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
		System.out.printf("%s set from properties%n.", propertyName);

		assert System.getProperty(propertyName).equals(property) : "System property not set.";
	}


	return isChanged;
}

/**
 *
 * @param properties
 */
public static void printProperties(Properties properties) {
	System.out.println("Properties :");
	properties.forEach((k, v) -> System.out.printf("Key: %s. Value: %s%n", k, v));
}

}






