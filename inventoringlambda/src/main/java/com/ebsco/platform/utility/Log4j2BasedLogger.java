package com.ebsco.platform.utility;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Properties;

public class Log4j2BasedLogger {
public static final Logger logger = LogManager.getLogger();

public static Logger getLogger() {
	return logger;
}

/**
 *
 * @param s3
 */
public static void logBuckets(AmazonS3 s3) {
	List<Bucket> buckets = s3.listBuckets();
	buckets.forEach(logger::info);
}

/**
 *
 * @param properties
 */
public static void logProperties(Properties properties) {
	System.out.println("Properties :");
	properties.forEach((k, v) -> logger.info("Key: {}. Value: {}", k, v));
}

/**
 *
 * @param args
 */
public static void main(String[] args) {
	logProperties(System.getProperties());
	logger.error("Check error");
}
}
