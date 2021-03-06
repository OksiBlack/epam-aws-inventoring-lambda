package com.ebsco.platform.aqa.tests;

import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.lambda.model.Runtime;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.ebsco.platform.aqa.utils.MiscTestHelperUtils;
import com.ebsco.platform.configuration.ConfigConstants;
import com.ebsco.platform.utils.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder.logger;
import static org.junit.jupiter.api.Assertions.*;

public class AWSParametersTest extends InventoringLambdaTest{


@DisplayName("Delete not empty bucketName. Exception expected")

@Test
public void testDeleteNotEmptyBucket() throws IOException {

	String bucket = reader.getProperty(ConfigConstants.P_NAME_AWS_BUCKET_NAME, ConfigConstants.DEFAULT_BUCKET_NAME);
	String unique = bucket + DASH + UUID.randomUUID();

	s3ClientHolder.createBucketIfNotExists(unique);

	File sampleFile = MiscTestHelperUtils.createSampleFile("aws-inventoring-lambda-temp", ".txt", 300);
	String expected = "The bucket you tried to delete is not empty";

	s3ClientHolder.putFileObjectToBucket(unique, sampleFile.toString(), sampleFile);
	logger.info("Trying to delete not empty bucket. Exception expected.");
	AmazonS3Exception ex = assertThrows(AmazonS3Exception.class, () -> {
		s3ClientHolder.deleteBucket(unique);

	}, expected);

	assertTrue(ex.getMessage()
			.contains(expected), expected);

}

@DisplayName("Create new bucket. Success")
@Test
public void testCreateBucketSuccess() {

	if (s3ClientHolder.doesBucketExists(bucketName)) {

		s3ClientHolder.deleteNotEmptyBucket(bucketName, false);
		assertFalse(s3ClientHolder.doesBucketExists(bucketName), "Bucket should be deleted by now.");
	}

	Bucket newBucket = s3ClientHolder.getAmazonS3()
			.createBucket(bucketName);

	logger.info(newBucket);

	assertTrue(s3ClientHolder.doesBucketExists(bucketName), "Bucket should exist now.");
	logger.info(newBucket);

}

@DisplayName("Not valid parameters passed as bucketName name.")
@Test
void testInvalidBucketNameSpecified() {

	logger.info("Passing invalid bucketName names to create bucketName request. Exceptions expected.");
	assertAll(
			() -> {
				String message = "Bucket name should not contain '_'";
				logger.info(message);
				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> s3ClientHolder.createNewBucket("underscores_name"), message);
				assertEquals(message, ex.getMessage());
			},

			() -> {
				String message = "Bucket name should be between 3 and 63 characters long";
				logger.info(message);

				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> s3ClientHolder.createNewBucket("sh"), message);
				assertEquals(message, ex.getMessage());

			},

			() -> {
				String message = "Bucket name must not be formatted as an IP Address";
				logger.info(message);

				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> s3ClientHolder.createNewBucket("192.168.1.5"), message);
				assertEquals(message, ex.getMessage());

			},

			() -> {

				String message = "Bucket name should not contain white space";
				logger.info(message);
				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> s3ClientHolder.createNewBucket("space there"), message);
				assertEquals(message, ex.getMessage());

			},

			() -> {
				String message = "Bucket name should not contain dashes next to periods";
				logger.info(message);
				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> s3ClientHolder.createNewBucket("name-with-dashes-after-period.-55"), message);
				assertEquals(message, ex.getMessage());

			},

			() -> {
				String message = "Bucket name should not begin with a '-'";
				logger.info(message);
				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> s3ClientHolder.createNewBucket("-with-dash"), message);
				assertEquals(message, ex.getMessage());

			},

			() -> {
				String message = "Bucket name should not contain '''";
				logger.info(message);
				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> s3ClientHolder.createNewBucket("quotes'in-st" + "'ring"), message);
				assertEquals(message, ex.getMessage());

			},

			() -> assertThrows(IllegalArgumentException.class, () -> s3ClientHolder.createNewBucket("end-with-dash-")),

			() -> {
				String message = "Bucket name should not end with '-' or " + "'.'";
				logger.info(message);
				IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> s3ClientHolder.createNewBucket("end-with."), message);
				assertEquals(message, ex.getMessage());

			});

}

@DisplayName("Create bucket. Null as bucketName name. Exception expected")
@Test
void testNullBucketName() {
	String bName = null;

	String message = "The bucket name parameter must be specified when creating a bucket";
	logger.info(message);

	IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
		s3ClientHolder.getAmazonS3()
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

				AmazonDynamoDBException ex = assertThrows(AmazonDynamoDBException.class, () -> dynamoDBClientHolder.createTableForIngestionLambda("sh"), message);

				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(message), ex.getMessage());

			},

			() -> {

				String message = "Member must satisfy regular expression pattern: [a-zA-Z0-9_.-]+";
				logger.info(message);
				AmazonDynamoDBException ex = assertThrows(AmazonDynamoDBException.class, () -> dynamoDBClientHolder.createTableForIngestionLambda("space there"), message);
				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(message), ex.getMessage());

			},

			() -> {
				String message = "Member must satisfy regular expression pattern: [a-zA-Z0-9_.-]+";
				logger.info(message);
				AmazonDynamoDBException ex = assertThrows(AmazonDynamoDBException.class, () -> dynamoDBClientHolder.createTableForIngestionLambda("quotes'in-st" + "'ring"), message);
				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(message), ex.getMessage());
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
		dynamoDBClientHolder.getAmazonDynamoDB()
				.createTable(createTableRequest);
	}, message);

	logger.info("Actual exception: {}", ex.getMessage());

	assertTrue(ex.getMessage()
			.contains(message), ex.getMessage());

}

/**
 *
 */
@DisplayName("Create table. Success")

@Test
public void testCreateTableSuccess() {

	String tableNameRandom = AWSParametersTest.tableName + "-" + UUID.randomUUID();
	CreateTableResult createTableResult = dynamoDBClientHolder.createTableForIngestionLambda(tableNameRandom);

	logger.info(createTableResult);

	assertTrue(dynamoDBClientHolder.isTableExists(tableNameRandom), "Table should exist");


}

@DisplayName("Create table. Already exists. Exception expected")

@Test
public void testCreateTableAlreadyExists() {

	boolean alreadyExists = dynamoDBClientHolder.isTableExists(tableName);
	if (!alreadyExists) {

		CreateTableResult createTableResult = dynamoDBClientHolder.createTableForIngestionLambda(tableName);

		logger.info("Table created: {} ", createTableResult);
		assertTrue(dynamoDBClientHolder.isTableExists(tableName));
	}

	String expected = "Table already exists";
	ResourceInUseException ex = assertThrows(com.amazonaws.services.dynamodbv2.model.ResourceInUseException.class, () -> {
		logger.info("Expecting exception. {}", expected);

		CreateTableResult createTableResult = dynamoDBClientHolder.createTableForIngestionLambda(tableName);

	}, expected);
	logger.info("Actual exception: {}", ex.getMessage());

	assertTrue(ex.getMessage()
			.contains(expected), ex.getMessage());

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
	code.withS3Bucket(bucketName);
	request.withFunctionName(newFunc)
			.withRole(lambdaRole)
			.withRuntime(Runtime.Java8)
			.withHandler(handlerName)
			.withCode(code);

	String expected = "Please do not provide other FunctionCode parameters when providing a ZipFile";
	InvalidParameterValueException ex = assertThrows(InvalidParameterValueException.class, () -> {
		logger.info(expected);
		lambdaClientHolder.getAwsLambdaClient()
				.createFunction(request);

	});
	logger.info("Actual exception: {}", ex.getMessage());

	assertTrue(ex.getMessage()
			.contains(expected), ex.getMessage());

}

@DisplayName("Should pass a valid parameter as //todo name.")

@Test
public void testCreateFunctionSuccess() throws IOException {

	Path path = FileUtils.findFirstDeeperInDirByTail(Paths.get("."), ConfigConstants.ZIP_EXTENSION);

	boolean functionAlreadyExists = lambdaClientHolder.isFunctionAlreadyExists(funcName);
	if (functionAlreadyExists) {
		DeleteFunctionResult deleteFunctionResult = lambdaClientHolder.deleteFunction(funcName);
		logger.info(deleteFunctionResult);

		assertFalse(lambdaClientHolder.isFunctionAlreadyExists(funcName), "Function should be deleted");
	}
	CreateFunctionResult functionWithTableNameAsEnvironmentVariable = lambdaClientHolder.createFunctionWithTableNameAsEnvironmentVariable(funcName, path, handlerName, lambdaRole, tableName);

	logger.info(functionWithTableNameAsEnvironmentVariable);

	assertTrue(lambdaClientHolder.isFunctionAlreadyExists(funcName), "Function should exist");

}

@DisplayName("Create Function. Exception Already Exists")
@Test
public void testCreateFunctionAlreadyExists() throws IOException {

	Path path = FileUtils.findFirstDeeperInDirByTail(Paths.get("."), ConfigConstants.ZIP_EXTENSION);

	boolean functionAlreadyExists = lambdaClientHolder.isFunctionAlreadyExists(funcName);
	if (!functionAlreadyExists) {
		CreateFunctionResult functionWithTableNameAsEnvironmentVariable = lambdaClientHolder.createFunctionWithTableNameAsEnvironmentVariable(funcName, path, handlerName, lambdaRole, tableName);
		logger.info("Function created: {} ", functionWithTableNameAsEnvironmentVariable);
		assertTrue(lambdaClientHolder.isFunctionAlreadyExists(funcName));
	}

	String expected = "Function already exist";
	ResourceConflictException ex = assertThrows(ResourceConflictException.class, () -> {
		logger.info("Expecting exception. {}", expected);

		lambdaClientHolder.createFunctionWithTableNameAsEnvironmentVariable(funcName, path, handlerName, lambdaRole, tableName);

	}, expected);
	logger.info("Actual exception: {}", ex.getMessage());

	assertTrue(ex.getMessage()
			.contains(expected), ex.getMessage());

}

@DisplayName("Create Function. Invalid zip file with function code")

@Test
public void testCreateFunctionNotInvalidZip() throws IOException {

	Path path = FileUtils.findFirstDeeperInDirByTail(Paths.get("."), ".xml");

	String expected = "Could not unzip uploaded file. Please check your file, then try to upload again.";
	InvalidParameterValueException ex = assertThrows(InvalidParameterValueException.class, () -> {

		lambdaClientHolder.createFunctionWithTableNameAsEnvironmentVariable(funcName, path, handlerName, lambdaRole, tableName);

	}, expected);

	logger.info("Actual exception: {}", ex.getMessage());

	assertTrue(ex.getMessage()
			.contains(expected), ex.getMessage());

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
		lambdaClientHolder.createFunctionWithTableNameAsEnvironmentVariable(funcName, path, handlerName, "fakerole", tableName);

	}, expected);
	logger.info("Actual exception: {}", ex.getMessage());

	assertTrue(ex.getMessage()
			.contains(expected), ex.getMessage());

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
		lambdaClientHolder.createFunctionWithTableNameAsEnvironmentVariable(newFunc, path, handlerName, fakeRole, tableName);

	}, expected);
	logger.info("Actual exception: {}", ex.getMessage());

	assertTrue(ex.getMessage()
			.contains(expected), ex.getMessage());

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

				AWSLambdaException ex = assertThrows(AWSLambdaException.class, () -> lambdaClientHolder.createFunctionWithTableNameAsEnvironmentVariable("192.168.1.5", path, handlerName, lambdaRole, tableName), errorTemplate);
				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(errorTemplate), ex.getMessage());
			},

			() -> {

				String message = "Spaces not allowed in function name";
				logger.info(message);
				AWSLambdaException ex = assertThrows(AWSLambdaException.class, () -> lambdaClientHolder.createFunctionWithTableNameAsEnvironmentVariable("space there", path, handlerName, lambdaRole, tableName), errorTemplate);
				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(errorTemplate), ex.getMessage());
			},

			() -> {
				String message = "name-with-dashes-after-period.-55";
				logger.info(message);
				AWSLambdaException ex = assertThrows(AWSLambdaException.class, () -> lambdaClientHolder.createFunctionWithTableNameAsEnvironmentVariable("name-with-dashes-after-period.-55", path, handlerName, lambdaRole, tableName), errorTemplate);
				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(errorTemplate), ex.getMessage());
			},

			() -> {
				String message = "Function name should not contain '''";
				logger.info(message);
				AWSLambdaException ex = assertThrows(AWSLambdaException.class, () -> lambdaClientHolder.createFunctionWithTableNameAsEnvironmentVariable("quotes'in-st-'ring", path, handlerName, lambdaRole, tableName), errorTemplate);
				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(errorTemplate), ex.getMessage());
			},

			() -> assertThrows(AWSLambdaException.class, () -> lambdaClientHolder.createFunctionWithTableNameAsEnvironmentVariable("end-with-dash-", path, handlerName, lambdaRole, tableName)),

			() -> {
				String message = "Function name should not end with '-' or " + "'.'";
				logger.info(message);
				AWSLambdaException ex = assertThrows(AWSLambdaException.class, () -> lambdaClientHolder.createFunctionWithTableNameAsEnvironmentVariable("end-with.", path, handlerName, lambdaRole, tableName), errorTemplate);
				logger.info("Actual exception: {}", ex.getMessage());

				assertTrue(ex.getMessage()
						.contains(errorTemplate), ex.getMessage());
			});
}
}