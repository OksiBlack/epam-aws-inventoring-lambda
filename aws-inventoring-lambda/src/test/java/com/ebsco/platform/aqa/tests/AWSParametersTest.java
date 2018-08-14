package com.ebsco.platform.aqa.tests;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.lambda.model.Runtime;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.ebsco.platform.aqa.utils.MiscTestHelperUtils;
import com.ebsco.platform.configuration.ConfigConstants;
import com.ebsco.platform.configuration.PropertiesReader;
import com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder;
import com.ebsco.platform.core.awsclientholders.AmazonLambdaClientHolder;
import com.ebsco.platform.core.awsclientholders.AmazonS3ClientHolder;
import com.ebsco.platform.core.awsclientholders.IFaceAWSClientHolder;
import com.ebsco.platform.core.helpers.AmazonFileTransferHelper;
import com.ebsco.platform.utils.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder.logger;
import static org.junit.jupiter.api.Assertions.*;

public class AWSParametersTest {

public static final String DASH = "-";

private static PropertiesReader reader = PropertiesReader.getInstance();

private static String funcName = reader.getProperty(ConfigConstants.P_NAME_AWS_LAMBDA_FUNCTION_NAME);
private static String tableName = reader.getProperty(ConfigConstants.P_NAME_TABLE_NAME);

private static String lambdaRole = reader.getProperty(ConfigConstants.P_NAME_AWS_LAMBDA_ROLE,
		ConfigConstants.DEFAULT_LAMBDA_ROLE);

private static String handlerName = reader.getProperty(ConfigConstants.P_NAME_AWS_LAMBDA_HANDLER,
		ConfigConstants.DEFAULT_LAMBDA_HANDLER);

private static Regions regions = IFaceAWSClientHolder.formRegion(reader.getProperty(ConfigConstants.P_NAME_AWS_REGION, ConfigConstants.DEFAULT_REGION.name()));
private static String bucket = reader.getProperty(ConfigConstants.P_NAME_AWS_BUCKET_NAME, ConfigConstants.DEFAULT_BUCKET_NAME);

private static AmazonDynamoDBClientHolder dynamoDBClient = new AmazonDynamoDBClientHolder(regions);
private static AmazonS3ClientHolder amazonS3ClientHolder = new AmazonS3ClientHolder(regions);
private static AmazonLambdaClientHolder amazonLambdaClient = new AmazonLambdaClientHolder(regions);

@BeforeEach
void setUp() {
	System.out.println();
}

@BeforeAll
public static void setUpBeforeAll() {

}

@DisplayName("Delete not empty bucket. Exception expected")

@Test
public void testDeleteNotEmptyBucket() throws IOException {

	String bucket = reader.getProperty(ConfigConstants.P_NAME_AWS_BUCKET_NAME, ConfigConstants.DEFAULT_BUCKET_NAME);
	String unique = bucket + DASH + UUID.randomUUID();

	amazonS3ClientHolder.createBucketIfNotExists(unique);

	Path sampleFile = MiscTestHelperUtils.createSampleFile("aws-inventoring-lambda-temp", ".txt", 300);
	String expected = "The bucket you tried to delete is not empty";

	amazonS3ClientHolder.putFileObjectToBucket(unique, sampleFile.toString(), sampleFile);
	logger.info("Trying to delete not empty bucket. Exception expected.");
	AmazonS3Exception ex = assertThrows(AmazonS3Exception.class, () -> {
		amazonS3ClientHolder.deleteBucket(unique);

	}, expected);

	assertTrue(ex.getMessage()
			.contains(expected), expected);

}

@DisplayName("Create new bucket. Success")
@Test
public void testCreateBucketSuccess() {

	if (amazonS3ClientHolder.doesBucketExists(bucket)) {

		amazonS3ClientHolder.deleteBucket(bucket);
		assertFalse(amazonS3ClientHolder.doesBucketExists(bucket), "Bucket should be deleted by now.");
	}

	Bucket newBucket = amazonS3ClientHolder.getAmazonS3()
			.createBucket(bucket);

	logger.info(newBucket);

	assertTrue(amazonS3ClientHolder.doesBucketExists(bucket), "Bucket should exist now.");
	logger.info(newBucket);

}

@DisplayName("Not valid parameters passed as bucket name.")
@Test
void testInvalidBucketNameSpecified() {

	logger.info("Passing invalid bucket names to create bucket request. Exceptions expected.");
	assertAll(
			() -> {
				String message = "Bucket name should not contain '_'";
				logger.info(message);
				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> amazonS3ClientHolder.createNewBucket("underscores_name"), message);
				assertEquals(message, ex.getMessage());
			},

			() -> {
				String message = "Bucket name should be between 3 and 63 characters long";
				logger.info(message);

				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> amazonS3ClientHolder.createNewBucket("sh"), message);
				assertEquals(message, ex.getMessage());

			},

			() -> {
				String message = "Bucket name must not be formatted as an IP Address";
				logger.info(message);

				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> amazonS3ClientHolder.createNewBucket("192.168.1.5"), message);
				assertEquals(message, ex.getMessage());

			},

			() -> {

				String message = "Bucket name should not contain white space";
				logger.info(message);
				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> amazonS3ClientHolder.createNewBucket("space there"), message);
				assertEquals(message, ex.getMessage());

			},

			() -> {
				String message = "Bucket name should not contain dashes next to periods";
				logger.info(message);
				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> amazonS3ClientHolder.createNewBucket("name-with-dashes-after-period.-55"), message);
				assertEquals(message, ex.getMessage());

			},

			() -> {
				String message = "Bucket name should not begin with a '-'";
				logger.info(message);
				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> amazonS3ClientHolder.createNewBucket("-with-dash"), message);
				assertEquals(message, ex.getMessage());

			},

			() -> {
				String message = "Bucket name should not contain '''";
				logger.info(message);
				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> amazonS3ClientHolder.createNewBucket("quotes'in-st" + "'ring"), message);
				assertEquals(message, ex.getMessage());

			},

			() -> assertThrows(IllegalArgumentException.class, () -> amazonS3ClientHolder.createNewBucket("end-with-dash-")),

			() -> {
				String message = "Bucket name should not end with '-' or " + "'.'";
				logger.info(message);
				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> amazonS3ClientHolder.createNewBucket("end-with."), message);
				assertEquals(message, ex.getMessage());

			});

}

@DisplayName("Create bucket. Null as bucket name. Exception expected")
@Test
void testNullBucketName() {
	String bName = null;

	String message = "The bucket name parameter must be specified when creating a bucket";
	logger.info(message);

	IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
		amazonS3ClientHolder.getAmazonS3()
				.createBucket(bName);
	}, message);

	assertEquals(message, ex.getMessage());

}

/**
 *
 */
@DisplayName("Invalid table name. Exception expected")
@Test
public void testCreateTableInvalidInput() {

	logger.info("Passing invalid bucket names to create bucket request. Exceptions expected.");
	assertAll(

			() -> {
				String message = "TableName must be at least 3 characters long and at most 255 characters long";
				logger.info(message);

				AmazonDynamoDBException ex = assertThrows(AmazonDynamoDBException.class, () -> dynamoDBClient.createTableForIngestionLambda("sh"), message);

				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(message));

			},

			() -> {

				String message = "Member must satisfy regular expression pattern: [a-zA-Z0-9_.-]+";
				logger.info(message);
				AmazonDynamoDBException ex = assertThrows(AmazonDynamoDBException.class, () -> dynamoDBClient.createTableForIngestionLambda("space there"), message);
				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(message));

			},

			() -> {
				String message = "Member must satisfy regular expression pattern: [a-zA-Z0-9_.-]+";
				logger.info(message);
				AmazonDynamoDBException ex = assertThrows(AmazonDynamoDBException.class, () -> dynamoDBClient.createTableForIngestionLambda("quotes'in-st" + "'ring"), message);
				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(message));
			},

			() -> {
				String message = "Member must satisfy regular expression pattern: [a-zA-Z0-9_.-]+";
				logger.info(message);
				AmazonDynamoDBException ex = assertThrows(AmazonDynamoDBException.class, () -> dynamoDBClient.createTableForIngestionLambda(null), message);
				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(message));
			}

	);

}

@DisplayName("Create table. Null as table name. Exception expected")
@Test
void testNullTableName() {

	String message = "The parameter 'TableName' is required but was not present in the request";
	logger.info(message);

	CreateTableRequest createTableRequest = new CreateTableRequest();
	AmazonDynamoDBException ex = assertThrows(AmazonDynamoDBException.class, () -> {
		dynamoDBClient.getAmazonDynamoDB()
				.createTable(createTableRequest);
	}, message);

	logger.info("Actual exception: {}", ex.getMessage());

	assertTrue(ex.getMessage()
			.contains(message));

}

/**
 *
 */
@DisplayName("Create table. Success")

@Test
public void testCreateTableSuccess() {

	String tableNameRandom = AWSParametersTest.tableName + "-" + UUID.randomUUID();
	CreateTableResult createTableResult = dynamoDBClient.createTableForIngestionLambda(tableNameRandom);

	logger.info(createTableResult);

	assertTrue(dynamoDBClient.isTableExists(tableNameRandom));

}

@DisplayName("Create table. Already exists. Exception expected")

@Test
public void testCreateTableAlreadyExists() {

	boolean alreadyExists = dynamoDBClient.isTableExists(tableName);
	if (!alreadyExists) {

		CreateTableResult createTableResult = dynamoDBClient.createTableForIngestionLambda(tableName);

		logger.info("Table created: {} ", createTableResult);
		assertTrue(dynamoDBClient.isTableExists(tableName));
	}

	String expected = "Table already exists";
	ResourceInUseException ex = assertThrows(com.amazonaws.services.dynamodbv2.model.ResourceInUseException.class, () -> {
		logger.info("Expecting exception. {}", expected);

		CreateTableResult createTableResult = dynamoDBClient.createTableForIngestionLambda(tableName);

	}, expected);
	logger.info("Actual exception: {}", ex.getMessage());

	assertTrue(ex.getMessage()
			.contains(expected));

}

/**
 * @throws IOException
 */
@DisplayName("Create function. Zip and FuncCode parameters specified. Exception expected.")
@Test
public void testCreateFunctionInvalidFunctionCodeParamsAndZip() throws IOException {

	String functionArn = null;
	Path path = FileUtils.findFirstDeeperInDirByTail(Paths.get("."), ConfigConstants.ZIP_EXTENSION);
	String newFunc = funcName + DASH + UUID.randomUUID();

	CreateFunctionRequest request = new CreateFunctionRequest();

	FunctionCode code = new FunctionCode();
	code
			.withZipFile(FileUtils.bytesFromFileToByteBuffer(path));
	code.withS3Bucket(bucket);
	request.withFunctionName(newFunc)
			.withRole(lambdaRole)
			.withRuntime(Runtime.Java8)
			.withHandler(handlerName)
			.withCode(code);

	String expected = "Please do not provide other FunctionCode parameters when providing a ZipFile";
	InvalidParameterValueException ex = assertThrows(InvalidParameterValueException.class, () -> {
		logger.info(expected);
		amazonLambdaClient.getAwsLambdaClient()
				.createFunction(request);

	});
	logger.info("Actual exception: {}", ex.getMessage());

	assertTrue(ex.getMessage()
			.contains(expected));

}

@DisplayName("Should pass a valid parameter as //todo name.")

@Test
public void testCreateFunctionSuccess() throws IOException {

	Path path = FileUtils.findFirstDeeperInDirByTail(Paths.get("."), ConfigConstants.ZIP_EXTENSION);

	boolean functionAlreadyExists = amazonLambdaClient.isFunctionAlreadyExists(funcName);
	if (functionAlreadyExists) {
		DeleteFunctionResult deleteFunctionResult = amazonLambdaClient.deleteFunction(funcName);
		logger.info(deleteFunctionResult);

		assertFalse(amazonLambdaClient.isFunctionAlreadyExists(funcName));
	}
	CreateFunctionResult functionWithTableNameAsEnvironmentVariable = amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable(funcName, path, handlerName, lambdaRole, tableName);

	logger.info(functionWithTableNameAsEnvironmentVariable);

	assertTrue(amazonLambdaClient.isFunctionAlreadyExists(funcName));

}

@DisplayName("Create Function. Exception Already Exists")
@Test
public void testCreateFunctionAlreadyExists() throws IOException {

	Path path = FileUtils.findFirstDeeperInDirByTail(Paths.get("."), ConfigConstants.ZIP_EXTENSION);

	boolean functionAlreadyExists = amazonLambdaClient.isFunctionAlreadyExists(funcName);
	if (!functionAlreadyExists) {
		CreateFunctionResult functionWithTableNameAsEnvironmentVariable = amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable(funcName, path, handlerName, lambdaRole, tableName);
		logger.info("Function created: {} ", functionWithTableNameAsEnvironmentVariable);
		assertTrue(amazonLambdaClient.isFunctionAlreadyExists(funcName));
	}

	String expected = "Function already exist";
	ResourceConflictException ex = assertThrows(ResourceConflictException.class, () -> {
		logger.info("Expecting exception. {}", expected);

		amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable(funcName, path, handlerName, lambdaRole, tableName);

	}, expected);
	logger.info("Actual exception: {}", ex.getMessage());

	assertTrue(ex.getMessage()
			.contains(expected));

}

@DisplayName("Create Function. Invalid zip file with function code")

@Test
public void testCreateFunctionNotInvalidZip() throws IOException {

	Path path = FileUtils.findFirstDeeperInDirByTail(Paths.get("."), ".xml");

	String expected = "Could not unzip uploaded file. Please check your file, then try to upload again.";
	InvalidParameterValueException ex = assertThrows(InvalidParameterValueException.class, () -> {

		amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable(funcName, path, handlerName, lambdaRole, tableName);

	}, expected);

	logger.info("Actual exception: {}", ex.getMessage());

	assertTrue(ex.getMessage()
			.contains(expected));

}

/**
 * @throws IOException
 */
@DisplayName("Create Function. Role Invalid name")
@Test
public void testCreateFunctionRoleNameIllegal() throws IOException {

	Path path = FileUtils.findFirstDeeperInDirByTail(Paths.get("."), ConfigConstants.ZIP_EXTENSION);

	String expected = "failed to satisfy constraint: Member must satisfy regular expression pattern: arn:(aws[a-zA-Z-]*)?:iam::\\d{12}:role/?[a-zA-Z_0-9+=,.@\\-_/]+";
	AWSLambdaException ex = assertThrows(AWSLambdaException.class, () -> {

		logger.info("Expecting exception about invalid role value.");
		amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable(funcName, path, handlerName, "fakerole", tableName);

	}, expected);
	logger.info("Actual exception: {}", ex.getMessage());

	assertTrue(ex.getMessage()
			.contains(expected));

}

/**
 * @throws IOException
 */
@DisplayName("Create Function. Role doesn't not exist")
@Test
public void testCreateFunctionNotExistingRole() throws IOException {

	Path path = FileUtils.findFirstDeeperInDirByTail(Paths.get("."), ConfigConstants.ZIP_EXTENSION);

	String newFunc = funcName + DASH + UUID.randomUUID();

	String fakeRole = "arn:aws:iam::501805042275:role/aws-lambda-full-access-not-exists";
	String expected = "The role defined for the function cannot be assumed by Lambda";
	AWSLambdaException ex = assertThrows(AWSLambdaException.class, () -> {

		logger.info("Expecting exception about invalid role value.");
		amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable(newFunc, path, handlerName, fakeRole, tableName);

	}, expected);
	logger.info("Actual exception: {}", ex.getMessage());

	assertTrue(ex.getMessage()
			.contains(expected));

}

@DisplayName("Create Function. Invalid function name. Exception expected")
@Test
public void testCreateFunctionInvalidParams() throws IOException {

	Path path = FileUtils.findFirstDeeperInDirByTail(Paths.get("."), ConfigConstants.ZIP_EXTENSION);

	String errorTemplate = "'functionName' failed to satisfy constraint: Member must satisfy regular expression pattern: (arn:(aws[a-zA-Z-]*)?:lambda:)?([a-z]{2}((-gov)|(-iso(b?)))?-[a-z]+-\\d{1}:)?(\\d{12}:)?(function:)?([a-zA-Z0-9-_]+)(:" +
			"(\\$LATEST|[a-zA-Z0-9-_]+))?";

	logger.info("Passing invalid function names to create function request. Exceptions expected.");
	assertAll(

			() -> {
				String message = "Function name should not be 192.168.1.5";
				logger.info(message);

				AWSLambdaException ex = assertThrows(AWSLambdaException.class, () -> amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable("192.168.1.5", path, handlerName, lambdaRole, tableName), errorTemplate);
				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(errorTemplate));
			},

			() -> {

				String message = "Spaces not allowed in function name";
				logger.info(message);
				AWSLambdaException ex = assertThrows(AWSLambdaException.class, () -> amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable("space there", path, handlerName, lambdaRole, tableName), errorTemplate);
				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(errorTemplate));
			},

			() -> {
				String message = "name-with-dashes-after-period.-55";
				logger.info(message);
				AWSLambdaException ex = assertThrows(AWSLambdaException.class, () -> amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable("name-with-dashes-after-period.-55", path, handlerName, lambdaRole, tableName), errorTemplate);
				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(errorTemplate));
			},

			() -> {
				String message = "Function name should not contain '''";
				logger.info(message);
				AWSLambdaException ex = assertThrows(AWSLambdaException.class, () -> amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable("quotes'in-st-'ring", path, handlerName, lambdaRole, tableName), errorTemplate);
				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(errorTemplate));
			},

			() -> assertThrows(AWSLambdaException.class, () -> amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable("end-with-dash-", path, handlerName, lambdaRole, tableName)),

			() -> {
				String message = "Function name should not end with '-' or " + "'.'";
				logger.info(message);
				AWSLambdaException ex = assertThrows(AWSLambdaException.class, () -> amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable("end-with.", path, handlerName, lambdaRole, tableName), errorTemplate);
				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(errorTemplate));
			});
}
}