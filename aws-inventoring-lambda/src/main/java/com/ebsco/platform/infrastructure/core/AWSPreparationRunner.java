package com.ebsco.platform.infrastructure.core;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeResult;
import com.ebsco.platform.infrastructure.configuration.ConfigConstants;
import com.ebsco.platform.infrastructure.configuration.PropertiesReader;
import com.ebsco.platform.infrastructure.core.awsclients.AmazonDynamoDBClient;
import com.ebsco.platform.infrastructure.core.awsclients.AmazonLambdaClient;
import com.ebsco.platform.infrastructure.core.awsclients.AmazonS3Client;
import com.ebsco.platform.infrastructure.core.awsclients.IFaceAWSClient;
import com.ebsco.platform.infrastructure.utility.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class AWSPreparationRunner {
public static final Logger logger = LogManager.getLogger();

public static void main(String[] args) {


	try {
		PropertiesReader reader = PropertiesReader.getInstance();

		Properties properties = reader.getProperties();

		String regionProp = reader.getProperty(ConfigConstants.P_NAME_AWS_REGION,
				ConfigConstants.DEFAULT_REGION.getName());

		Regions region = IFaceAWSClient.formRegion(regionProp);


		AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(region);
		String tableName = properties.getProperty(ConfigConstants.P_NAME_TABLE_NAME);

		if (tableName == null) {
			throw new IllegalArgumentException("Table name must be provided.");
		}

		boolean tableIfNotExists = dynamoDBClient.createTableIfNotExists(tableName);
		String msg = tableIfNotExists ? "Table created" : "Table already exists. No action";
		logger.info(msg);

		AmazonLambdaClient lambdaClient = new AmazonLambdaClient(region);

		String funcName = reader.getProperty(ConfigConstants.P_NAME_AWS_LAMBDA_FUNCTION_NAME);

		Path path = null;
		path = FileUtils.findFirstDeeperInDirByTail(Paths.get("."), ConfigConstants.ZIP_EXTENSION);


		String lambdaRole = reader.getProperty(ConfigConstants.P_NAME_AWS_LAMBDA_ROLE,
				ConfigConstants.DEFAULT_LAMBDA_ROLE);

		String defaultLambdaHandler = reader.getProperty(ConfigConstants.P_NAME_AWS_LAMBDA_HANDLER,
				ConfigConstants.DEFAULT_LAMBDA_HANDLER);
		String functionArn = null;
		if (lambdaClient.isFunctionAlreadyExists(funcName)) {
			UpdateFunctionCodeResult updateFunctionCodeResult = lambdaClient.updateFunctionCode(funcName, path);
			functionArn = updateFunctionCodeResult.getFunctionArn();
			logger.info("Code updated. Function name: {}. Function Arn:{}.", updateFunctionCodeResult.getFunctionName(),
					functionArn);
		} else {
			CreateFunctionResult createFunctionResult = lambdaClient.createFunction(funcName, path, defaultLambdaHandler,
					lambdaRole);
			logger.info("Function created. Function name: {}. Function Arn:{}.",
					createFunctionResult.getFunctionName(), createFunctionResult.getFunctionArn());
			functionArn = createFunctionResult.getFunctionArn();

		}

		AmazonS3Client s3Client = new AmazonS3Client(region);

		String bName = properties.getProperty(ConfigConstants.P_NAME_AWS_BUCKET_NAME);
		if (bName == null) {
			throw new IllegalArgumentException("Bucket name must be provided.");
		}

		s3Client.createBucketIfNotExists(bName);
		lambdaClient.addPermissionForS3ToInvokeLambda(funcName);
		s3Client.addBucketNotificationConfiguration(bName, functionArn);


	}
	catch (AmazonServiceException ase) {
		logger.error("Caught an AmazonServiceException. Request made to Amazon S3 was rejected with an error response for some reason.");
		logger.error("Error Message:    " + ase.getMessage());
		logger.error("HTTP Status Code: " + ase.getStatusCode());
		logger.error("AWS Error Code:   " + ase.getErrorCode());
		logger.error("Error Type:       " + ase.getErrorType());
		logger.error("Request ID:       " + ase.getRequestId());
		logger.error(ase.getMessage(), ase);
		System.exit(-1);

	}
	catch (AmazonClientException ace) {
		logger.error("Caught an AmazonClientException. Internal problem while trying to communicate with S3.");
		logger.error("Error Message: " + ace.getMessage());
		System.exit(-1);

	}
	catch (Exception e) {
		logger.error(e.getMessage(), e);
		System.exit(-1);
	}

}

}

