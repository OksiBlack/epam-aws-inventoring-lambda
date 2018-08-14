package com.ebsco.platform.aqa.tests;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder;
import com.ebsco.platform.aqa.utils.AWSInteractionHelper;
import com.ebsco.platform.core.helpers.AmazonFileTransferHelper;
import com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder;
import com.ebsco.platform.core.awsclientholders.AmazonLambdaClientHolder;
import com.ebsco.platform.core.awsclientholders.AmazonS3ClientHolder;
import com.ebsco.platform.core.awsclientholders.IFaceAWSClientHolder;
import com.ebsco.platform.configuration.ConfigConstants;
import com.ebsco.platform.configuration.PropertiesReader;
import com.ebsco.platform.infrastructure.inventoringlambda.Application;
import com.ebsco.platform.utils.DateTimeUtils;
import com.ebsco.platform.utils.FileUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder.ORIGIN_TIME_STAMP;
import static com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder.PACKAGE_ID;

class AmazonS3FileUploadTest {
public static final String BIG_FILE_NAME = "DesktopImages-20180731T115600Z-001.zip";
public static final String SMALL_FILE_NAME = "log4j2.xml";
private static PropertiesReader reader = PropertiesReader.getInstance();
private static Regions regions = IFaceAWSClientHolder.formRegion(reader.getProperty(ConfigConstants.P_NAME_AWS_REGION,
		ConfigConstants.DEFAULT_REGION.name()));
private static AmazonDynamoDBClientHolder amazonDynamoDBClientHolder;
private static AmazonS3ClientHolder amazonS3ClientHolder;
private static AmazonLambdaClientHolder amazonLambdaClient;

@BeforeEach
void setUp() {
	System.out.println("AmazonS3FileUploadTest.setUp");

}

@BeforeAll
public static void setUpBeforeAll() {

	System.out.println(System.getenv(Application.DYNAMODB_TABLE));
	amazonDynamoDBClientHolder = new AmazonDynamoDBClientHolder(regions);
	amazonS3ClientHolder = new AmazonS3ClientHolder(regions);
	amazonLambdaClient = new AmazonLambdaClientHolder(regions);
	System.out.println("AmazonS3FileUploadTest.setUpBeforeAll");
}


@AfterEach
void tearDown() {
	System.out.println("AmazonS3FileUploadTest.tearDown");
}

@AfterAll
public static void tearDownAfterAll() {
	amazonDynamoDBClientHolder.shutdown();
	amazonLambdaClient.shutdown();
	amazonS3ClientHolder.shutdown();
	System.out.println("AmazonS3FileUploadTest.tearDownAfterAll");
}

@Test
void uploadFile() throws IOException, InterruptedException {


	AmazonFileTransferHelper h = new AmazonFileTransferHelper(amazonS3ClientHolder.getAmazonS3());
	String bucket = PropertiesReader.getInstance()
			.getProperty(ConfigConstants.P_NAME_AWS_BUCKET_NAME);
/*

	Path tempFile = Files.createTempFile("temp-", ".txt");
	try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
		writer.write("gdfddfgdfgdfg");

		File file = tempFile.toFile();
		h.uploadFile(bucket, null, file);

		file.deleteOnExit();

	}
*/


	Path log4g2Xml = FileUtils.findFirstDeeperInDirByName(Paths.get("."), SMALL_FILE_NAME);

h.uploadFile(bucket,null,log4g2Xml.toFile());

}

@Test
void uploadBigFile() throws IOException, InterruptedException {


	AmazonFileTransferHelper h = new AmazonFileTransferHelper(amazonS3ClientHolder.getAmazonS3());
	String bucket = PropertiesReader.getInstance()
			.getProperty(ConfigConstants.P_NAME_AWS_BUCKET_NAME);
/*

	Path tempFile = Files.createTempFile("temp-", ".txt");
	try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
		writer.write("gdfddfgdfgdfg");

		File file = tempFile.toFile();
		h.uploadFile(bucket, null, file);

		file.deleteOnExit();

	}
*/


	Path log4g2Xml = FileUtils.findFirstDeeperInDirByName(Paths.get("."), BIG_FILE_NAME    );

	h.uploadFile(bucket,null,log4g2Xml.toFile());

}

@Test
void uploadFileList() {
	System.out.println("hello");
}

@Test
void uploadDirectory() {
}

/**
 *
 */
@DisplayName("queryWithFilePathGlobalIndex  //todo name.")
@Test
public void testCreateTableSuccess() {
	String tableName = reader.getProperty(ConfigConstants.P_NAME_TABLE_NAME, ConfigConstants.DEFAULT_DYNAMODB_TABLE_NAME);
	Table table = amazonDynamoDBClientHolder.getTable(tableName);

/*	ExpressionSpecBuilder num2 = new ExpressionSpecBuilder().withCondition(ExpressionSpecBuilder.N
			("num2")
			.between(0, 100));*/

	String itemPackId = "1b50b4e7-5af6-35c7-ac5a-c950e90bb269";

	ItemCollection<String> collection = amazonDynamoDBClientHolder.queryByPrimaryKey(table, PACKAGE_ID, itemPackId);

	for (Item item : collection) {
		System.out.println(item);
	}

	//	GetItemRequest item1 = dynamoDBClient.getItem(tableName, PACKAGE_ID, itemPackId);

	//	String s = item1.toString();

	ItemCollection<QueryOutcome> items = AWSInteractionHelper.queryWithFilePathGlobalIndex(tableName, "report.pdf", amazonDynamoDBClientHolder);

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
}