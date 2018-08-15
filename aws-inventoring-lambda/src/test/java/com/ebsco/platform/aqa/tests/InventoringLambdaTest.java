package com.ebsco.platform.aqa.tests;

import com.amazonaws.regions.Regions;
import com.ebsco.platform.configuration.ConfigConstants;
import com.ebsco.platform.configuration.ConfigPropertiesReader;
import com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder;
import com.ebsco.platform.core.awsclientholders.AmazonLambdaClientHolder;
import com.ebsco.platform.core.awsclientholders.AmazonS3ClientHolder;
import com.ebsco.platform.core.awsclientholders.IFaceAWSClientHolder;
import org.junit.jupiter.api.AfterAll;




public abstract class InventoringLambdaTest {

public static final String DASH = "-";

protected static ConfigPropertiesReader reader = ConfigPropertiesReader.getInstance();

protected static Regions regions = IFaceAWSClientHolder.formRegion(reader.getProperty(ConfigConstants.P_NAME_AWS_REGION, ConfigConstants.DEFAULT_REGION.name()));

protected static AmazonDynamoDBClientHolder dynamoDBClientHolder = new AmazonDynamoDBClientHolder(regions);
protected static AmazonS3ClientHolder s3ClientHolder = new AmazonS3ClientHolder(regions);
protected static AmazonLambdaClientHolder lambdaClientHolder = new AmazonLambdaClientHolder(regions);

protected static String funcName = reader.getProperty(ConfigConstants.P_NAME_AWS_LAMBDA_FUNCTION_NAME);
protected static String tableName = reader.getProperty(ConfigConstants.P_NAME_TABLE_NAME);
protected static String lambdaRole = reader.getProperty(ConfigConstants.P_NAME_AWS_LAMBDA_ROLE, ConfigConstants.DEFAULT_LAMBDA_ROLE);
protected static String handlerName = reader.getProperty(ConfigConstants.P_NAME_AWS_LAMBDA_HANDLER, ConfigConstants.DEFAULT_LAMBDA_HANDLER);
protected static String bucketName = reader.getProperty(ConfigConstants.P_NAME_AWS_BUCKET_NAME, ConfigConstants.DEFAULT_BUCKET_NAME);


@AfterAll
public static void tearDownAfterAll() {
	dynamoDBClientHolder.shutdown();
	lambdaClientHolder.shutdown();
	s3ClientHolder.shutdown();
	System.out.println("AmazonS3FileUploadTest.tearDownAfterAll");
}
}
