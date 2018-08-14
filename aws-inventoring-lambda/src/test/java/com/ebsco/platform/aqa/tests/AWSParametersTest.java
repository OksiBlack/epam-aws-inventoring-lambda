package com.ebsco.platform.aqa.tests;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.ebsco.platform.aqa.utils.AWSInteractionHelper;
import com.ebsco.platform.aqa.utils.MiscTestHelperUtils;
import com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder;
import com.ebsco.platform.core.awsclientholders.AmazonLambdaClientHolder;
import com.ebsco.platform.core.awsclientholders.AmazonS3ClientHolder;
import com.ebsco.platform.core.awsclientholders.IFaceAWSClientHolder;
import com.ebsco.platform.configuration.ConfigConstants;
import com.ebsco.platform.configuration.PropertiesReader;
import com.ebsco.platform.core.helpers.AmazonFileTransferHelper;
import com.ebsco.platform.utils.DateTimeUtils;

import com.ebsco.platform.utils.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder.*;
import static org.junit.jupiter.api.Assertions.*;

public class AWSParametersTest {

public static final String DASH = "-";


private static PropertiesReader reader = PropertiesReader.getInstance();

private static String funcName = reader.getProperty(ConfigConstants.P_NAME_AWS_LAMBDA_FUNCTION_NAME);
private static 		String tableName = reader.getProperty(ConfigConstants.P_NAME_TABLE_NAME);



private static String lambdaRole = reader.getProperty(ConfigConstants.P_NAME_AWS_LAMBDA_ROLE,
		ConfigConstants.DEFAULT_LAMBDA_ROLE);

private static String handlerName = reader.getProperty(ConfigConstants.P_NAME_AWS_LAMBDA_HANDLER,
		ConfigConstants.DEFAULT_LAMBDA_HANDLER);
String functionArn = null;
private static Regions regions = IFaceAWSClientHolder.formRegion(reader.getProperty(ConfigConstants.P_NAME_AWS_REGION, ConfigConstants.DEFAULT_REGION.name()));
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
public void testDeleteNotEmpty() throws IOException {

	String bucket = reader.getProperty(ConfigConstants.P_NAME_AWS_BUCKET_NAME, ConfigConstants.DEFAULT_BUCKET_NAME);
	String unique = bucket + DASH + UUID.randomUUID();

	amazonS3ClientHolder.createBucketIfNotExists(unique);

	AmazonFileTransferHelper helper = new AmazonFileTransferHelper(amazonS3ClientHolder.getAmazonS3());

	Path sampleFile = MiscTestHelperUtils.createSampleFile("aws-inventoring-lambda-temp", ".txt", 300);
	String expected = "The bucket you tried to delete is not empty";

	amazonS3ClientHolder.putFileObjectToBucket(unique, sampleFile.toString(), sampleFile);
	logger.info("Trying to delete not empty bucket. Exception expected.");
	AmazonS3Exception ex = assertThrows(AmazonS3Exception.class, () -> {
		amazonS3ClientHolder.deleteBucket(unique);

	}, expected);

	assertTrue(ex.getMessage().contains(expected), expected);

}

@DisplayName("Create new bucket. Success")
@Test
public void testCreateBucketSuccess() {
	String bucket = reader.getProperty(ConfigConstants.P_NAME_AWS_BUCKET_NAME, ConfigConstants.DEFAULT_BUCKET_NAME);

	if (amazonS3ClientHolder.doesBucketExists(bucket)) {

		amazonS3ClientHolder.deleteBucket(bucket);
		assertFalse(amazonS3ClientHolder.doesBucketExists(bucket), "Bucket should be deleted by now.");
	}

	Bucket newBucket = amazonS3ClientHolder.getAmazonS3()
			.createBucket(bucket);

	logger.info(newBucket);

	assertTrue(amazonS3ClientHolder.doesBucketExists(bucket), "Bucket should exist now.");
	logger.info(newBucket);



/*
<ul>
     *      <li>Bucket names should not contain underscores</li>
     *      <li>Bucket names should be between 3 and 63 characters long</li>
     *      <li>Bucket names should not end with a dash</li>
     *      <li>Bucket names cannot contain adjacent periods</li>
     *      <li>Bucket names cannot contain dashes next to periods (e.g.,
     *      "my-.bucket.com" and "my.-bucket" are invalid)</li>
     *      <li>Bucket names cannot contain uppercase characters</li>
     *  </ul>
 */


/*
  private static final int MIN_BUCKET_NAME_LENGTH = 3;
    private static final int MAX_BUCKET_NAME_LENGTH = 63;
                        "Bucket name must not be formatted as an IP Address"
                                            "Bucket name should not contain white space"

                        "Bucket name should not contain dashes next to periods"

                        "Bucket name should not contain dashes next to periods"

                            "Bucket name should not begin with a '-'"

                    "Bucket name should not contain '" + next + "'"

                "Bucket name should not end with '-' or '.'"
*/

}

@DisplayName("Not valid parameters passed as bucket name.")
@Test
void testInvalidBucketNameSpecified() {

	String[] invalidNames = {"underscores_name", "sh", "192.168.1.5", "space there", "name-with-dashes-after-period.-55", "-with-dash", "quotes'in-st'ring", "end-with-dash-", "end-with."};

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



@DisplayName("Null as bucket name to create.")
@Test
void testNullBucketName() {
	String bName =null;

	String message = "The bucket name parameter must be specified when creating a bucket";
	logger.info(message);

	IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
		amazonS3ClientHolder.getAmazonS3()
				.createBucket(bName);
	}, message);


	assertEquals(message, ex.getMessage());

}

@DisplayName("Should pass a valid parameter as //todo  name.")
@ParameterizedTest
@ValueSource(strings = {"Hel", "World"})
@Test
public void testCreateTableInvalidInput() {

}

@DisplayName("queryWithFilePathGlobalIndex  //todo name.")

@Test
public void testCreateTableSuccess() {
	String tableName = reader.getProperty(ConfigConstants.P_NAME_TABLE_NAME, ConfigConstants.DEFAULT_DYNAMODB_TABLE_NAME);
	Table table = dynamoDBClient.getTable(tableName);

/*	ExpressionSpecBuilder num2 = new ExpressionSpecBuilder().withCondition(ExpressionSpecBuilder.N
			("num2")
			.between(0, 100));*/

	String itemPackId = "1b50b4e7-5af6-35c7-ac5a-c950e90bb269";

	ItemCollection<String> collection = dynamoDBClient.queryByPrimaryKey(table, PACKAGE_ID, itemPackId);

	for (Item item : collection) {
		System.out.println(item);
	}

	//	GetItemRequest item1 = dynamoDBClient.getItem(tableName, PACKAGE_ID, itemPackId);

	//	String s = item1.toString();

	ItemCollection<QueryOutcome> items = AWSInteractionHelper.queryWithFilePathGlobalIndex(tableName, "report.pdf", dynamoDBClient);

	ItemCollection<?> col = table.query(PACKAGE_ID, itemPackId, new RangeKeyCondition(ORIGIN_TIME_STAMP).between(DateTimeUtils.getEpochMillis(), DateTimeUtils.getNowMillis()), new ExpressionSpecBuilder().buildForQuery());

	for (Item item : col) {
		System.out.println(item.toJSONPretty());
	}

	ItemCollection<?> co2l = table.query(PACKAGE_ID, itemPackId, new RangeKeyCondition(ORIGIN_TIME_STAMP).between(DateTimeUtils.getEpochMillis(), DateTimeUtils.getNowMillis()), new ExpressionSpecBuilder().buildForQuery());

	for (Item item : col) {
		System.out.println(item.toJSONPretty());
	}



	/*Iterator<Item> iterator = items.iterator();
	Item item = null;
	while (iterator.hasNext()) {
		item = iterator.next();
		System.out.println(item.toJSONPretty());
	}

*/
}

@DisplayName("Should pass a valid parameter as //todo  name.")
@ParameterizedTest
@ValueSource(strings = {"Hel", "World"})
@Test
public void testCreateFunctionInvalidInputWithZip() throws IOException {




	String functionArn = null;
	Path path = FileUtils.findFirstDeeperInDirByTail(Paths.get("."), ConfigConstants.ZIP_EXTENSION);
	amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable(funcName,path, handlerName,lambdaRole,tableName);

}

@DisplayName("Should pass a valid parameter as //todo name.")

@Test
public void testCreateFunctionSuccess() throws IOException {


	Path path = FileUtils.findFirstDeeperInDirByTail(Paths.get("."), ConfigConstants.ZIP_EXTENSION);

	boolean functionAlreadyExists = amazonLambdaClient.isFunctionAlreadyExists(funcName);
	if(functionAlreadyExists){
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
	if(!functionAlreadyExists){
		CreateFunctionResult functionWithTableNameAsEnvironmentVariable = amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable(funcName, path, handlerName, lambdaRole, tableName);
		logger.info("Function created: {} ", functionWithTableNameAsEnvironmentVariable);
		assertTrue(amazonLambdaClient.isFunctionAlreadyExists(funcName));
	}


	String expected = "Function already exist";
	ResourceConflictException ex = assertThrows(ResourceConflictException.class, () -> {
		logger.info("Expecting exception. {}", expected);

		amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable(funcName, path, handlerName, lambdaRole, tableName);

	}, expected);

	assertTrue(ex.getMessage().contains(expected));
	logger.info("Actual exception: {}", ex.getMessage());


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

	assertTrue(ex.getMessage().contains(expected));


}


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

	assertTrue(ex.getMessage().contains(expected));



}


@DisplayName("Create Function. Role doesn't not exist")
@Test
public void testCreateFunctionNotExistingRole() throws IOException {


	Path path = FileUtils.findFirstDeeperInDirByTail(Paths.get("."), ConfigConstants.ZIP_EXTENSION);

	String newFunc = funcName + DASH + UUID.randomUUID();

	String fakeRole ="arn:aws:iam::501805042275:role/aws-lambda-full-access-not-exists";
	String expected = "The role defined for the function cannot be assumed by Lambda";
	AWSLambdaException ex = assertThrows(AWSLambdaException.class, () -> {

		logger.info("Expecting exception about invalid role value.");
		amazonLambdaClient.createFunctionWithTableNameAsEnvironmentVariable(newFunc, path, handlerName, fakeRole, tableName);

	}, expected);
	logger.info("Actual exception: {}", ex.getMessage());

	assertTrue(ex.getMessage().contains(expected));



}
}