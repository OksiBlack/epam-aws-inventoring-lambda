package com.ebsco.platform.aqa.tests;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder;
import com.ebsco.platform.aqa.utils.AWSInteractionHelper;
import com.ebsco.platform.configuration.ConfigConstants;
import com.ebsco.platform.configuration.ConfigPropertiesReader;
import com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder;
import com.ebsco.platform.core.awsclientholders.AmazonLambdaClientHolder;
import com.ebsco.platform.core.awsclientholders.AmazonS3ClientHolder;
import com.ebsco.platform.core.helpers.AmazonFileTransferHelper;
import com.ebsco.platform.infrastructure.inventoringlambda.Application;
import com.ebsco.platform.utils.DateTimeUtils;
import com.ebsco.platform.utils.FileUtils;
import com.ebsco.platform.utils.Formatter;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder.ORIGIN_TIME_STAMP;
import static com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder.PACKAGE_ID;
import static com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder.logger;

class AmazonS3FileUploadTest extends InventoringLambdaTest {

public static final String BIG_FILE_NAME = "DesktopImages-20180731T115600Z-001.zip";
public static final String SMALL_FILE_NAME = "log4j2.xml";

public static final String UPLOAD_FILE_NAME =reader.getProperty(ConfigConstants.P_NAME_AWS_UPLOADED_FILE_NAME, ConfigConstants.DEFAULT_CONFIG_FILE_NAME);


@BeforeEach
void setUp() {
	logger.trace("AmazonS3FileUploadTest.setUp");

}

@BeforeAll
public static void setUpBeforeAll() {

	dynamoDBClientHolder = new AmazonDynamoDBClientHolder(regions);
	s3ClientHolder = new AmazonS3ClientHolder(regions);
	lambdaClientHolder = new AmazonLambdaClientHolder(regions);
	logger.trace("AmazonS3FileUploadTest.setUpBeforeAll");
}


@AfterEach
void tearDown() {
	logger.trace("AmazonS3FileUploadTest.tearDown");
}

@AfterAll
public static void tearDownAfterAll() {
	dynamoDBClientHolder.shutdown();
	lambdaClientHolder.shutdown();
	s3ClientHolder.shutdown();
	logger.trace("AmazonS3FileUploadTest.tearDownAfterAll");
}

@Test
void uploadFile() throws IOException, InterruptedException {


	AmazonFileTransferHelper h = new AmazonFileTransferHelper(s3ClientHolder.getAmazonS3());
	String bucket = ConfigPropertiesReader.getInstance()
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


	AmazonFileTransferHelper h = new AmazonFileTransferHelper(s3ClientHolder.getAmazonS3());
	String bucket = ConfigPropertiesReader.getInstance()
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
	Table table = dynamoDBClientHolder.getTable(tableName);

/*	ExpressionSpecBuilder num2 = new ExpressionSpecBuilder().withCondition(ExpressionSpecBuilder.N
			("num2")
			.between(0, 100));*/

	String itemPackId = "1b50b4e7-5af6-35c7-ac5a-c950e90bb269";

	ItemCollection<String> collection = dynamoDBClientHolder.queryByPrimaryKey(table, PACKAGE_ID, itemPackId);

	logger.info(Formatter.formatItemCollection(collection, null, null));

	//	GetItemRequest item1 = dynamoDBClient.getItem(tableName, PACKAGE_ID, itemPackId);

	//	String s = item1.toString();

	ItemCollection<QueryOutcome> items = AWSInteractionHelper.queryWithFilePathGlobalIndex(tableName, "report.pdf", dynamoDBClientHolder);

	ItemCollection<?> col = table.query(PACKAGE_ID, itemPackId, new RangeKeyCondition(ORIGIN_TIME_STAMP).between(DateTimeUtils.getEpochMillis(), DateTimeUtils.getNowMillis()), new ExpressionSpecBuilder().buildForQuery());

	logger.info(Formatter.formatItemCollection(col, null, null));

	ItemCollection<?> co2l = table.query(PACKAGE_ID, itemPackId, new RangeKeyCondition(ORIGIN_TIME_STAMP).between(DateTimeUtils.getEpochMillis(), DateTimeUtils.getNowMillis()), new ExpressionSpecBuilder().buildForQuery());

	logger.info(Formatter.formatItemCollection(co2l, null, null));



	/*Iterator<Item> iterator = items.iterator();
	Item item = null;
	while (iterator.hasNext()) {
		item = iterator.next();
		System.out.println(item.toJSONPretty());
	}

*/
}
}