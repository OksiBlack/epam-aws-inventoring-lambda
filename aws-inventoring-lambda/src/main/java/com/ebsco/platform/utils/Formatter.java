package com.ebsco.platform.utils;

import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class Formatter {
public static final Logger logger = LogManager.getLogger();

public static Logger getLogger() {
	return logger;
}

/**
 * @param map
 * @param messageBefore
 * @param messageAfter
 * @param <K>
 * @param <V>
 * @return
 */
public static <K, V> StringBuilder formatMapContents(Map<K, V> map, String messageBefore, String messageAfter) {
	StringBuilder builder = new StringBuilder();
	builder.append(messageBefore);
	map.forEach((k, v) -> builder.append("Key: ")
			.append(k)
			.append("Value: ")
			.append(v));

	builder.append(messageAfter);
	return builder;
}

/**
 * @param co2l
 * @param messageBefore
 * @param messageAfter
 * @return
 */
public static StringBuilder formatItemCollection(ItemCollection<?> co2l, String messageBefore, String messageAfter) {
	StringBuilder builder = new StringBuilder();
	if(messageBefore!=null) {
		builder.append(messageBefore);
	}
	co2l.forEach((it) -> builder.append(it.toJSONPretty()));
	if(messageAfter!=null) {
		builder.append(System.lineSeparator()).append(messageBefore);
	}
	return builder;
}

}
