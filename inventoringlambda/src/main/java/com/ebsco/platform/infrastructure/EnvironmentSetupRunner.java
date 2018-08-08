package com.ebsco.platform.infrastructure;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.ebsco.platform.infrastructure.configuration.*;

import com.ebsco.platform.infrastructure.core.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class EnvironmentSetupRunner {
public static final Logger logger = LogManager.getLogger();

public static void main(String[] args) {

	Path path = null;
	try {
		path = Files.find(Paths.get("."), Integer.MAX_VALUE, (p, a) -> p.endsWith(ConfigConstants.CONFIG_FILE_NAME)).findFirst().orElseThrow(() -> new IOException("Path ended with " + ConfigConstants.CONFIG_FILE_NAME + " not found."));
	} catch (IOException e) {
		logger.error(e);
		System.exit(-1);
	}


	try {
		PropertiesReader reader = new PropertiesReader(path);
		reader.init();


		Properties properties = reader.getProperties();
		AmazonS3Client s3Client = new AmazonS3Client();

		String bName = properties.getProperty(ConfigConstants.P_NAME_AWS_BUCKET_NAME);
		if (bName == null) {
			throw new IllegalArgumentException("Bucket name must be provided.");
		}

		s3Client.createBucketIfNotExists(bName);


		AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient();
		String tableName = properties.getProperty(ConfigConstants.P_NAME_TABLE_NAME);

		if(tableName==null){
			throw new IllegalArgumentException("Table name must be provided.");
		}

		boolean tableIfNotExists = dynamoDBClient.createTableIfNotExists(tableName);
		String msg = tableIfNotExists ? "Table created" : "Table already exists. No action";
		logger.info(msg);


	} catch (IOException e) {
		logger.error(e);
	} catch (AmazonServiceException ase) {
		logger.error("Caught an AmazonServiceException, which means your request made it to Amazon S3, but was rejected with an error response for some reason.");
		logger.error("Error Message:    " + ase.getMessage());
		logger.error("HTTP Status Code: " + ase.getStatusCode());
		logger.error("AWS Error Code:   " + ase.getErrorCode());
		logger.error("Error Type:       " + ase.getErrorType());
		logger.error("Request ID:       " + ase.getRequestId());
	} catch (AmazonClientException ace) {
		logger.error("Caught an AmazonClientException, which means the client encountered a serious internal problem while trying to communicate with S3, such as not being able to access the network.");
		logger.error("Error Message: " + ace.getMessage());
	}
}

}
