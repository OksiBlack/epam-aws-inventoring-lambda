package com.ebsco.platform.aqa.tests;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.Upload;
import com.ebsco.platform.aqa.utils.AWSInteractionHelper;
import com.ebsco.platform.aqa.utils.MiscTestHelperUtils;
import com.ebsco.platform.configuration.ConfigConstants;
import com.ebsco.platform.configuration.ConfigPropertiesReader;
import com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder;
import com.ebsco.platform.core.awsclientholders.AmazonLambdaClientHolder;
import com.ebsco.platform.core.awsclientholders.AmazonS3ClientHolder;
import com.ebsco.platform.core.helpers.AmazonFileTransferHelper;

import com.ebsco.platform.utils.DateTimeUtils;
import com.ebsco.platform.utils.FileUtils;
import com.ebsco.platform.utils.Formatter;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder.ORIGIN_TIME_STAMP;
import static com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder.PACKAGE_ID;
import static com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder.logger;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmazonS3FileUploadTest extends InventoringLambdaTest {

public static final String BIG_FILE_NAME = "DesktopImages-20180731T115600Z-001.zip";
public static final String SMALL_FILE_NAME = "log4j2.xml";

public static final String UPLOAD_FILE_NAME = reader.getProperty(ConfigConstants.P_NAME_AWS_UPLOADED_FILE_NAME, ConfigConstants.DEFAULT_CONFIG_FILE_NAME);

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
void testUploadFile() throws IOException, InterruptedException {

	AmazonFileTransferHelper h = new AmazonFileTransferHelper(s3ClientHolder.getAmazonS3());
	String bucket = ConfigPropertiesReader.getInstance()
			.getProperty(ConfigConstants.P_NAME_AWS_BUCKET_NAME);

	Path file = FileUtils.findFirstDeeperInDirByName(Paths.get("."), SMALL_FILE_NAME);

	h.uploadFile(bucket, null, file.toFile());



}

@Test
void testUploadFile2() throws IOException, InterruptedException {

	AmazonFileTransferHelper h = new AmazonFileTransferHelper(s3ClientHolder.getAmazonS3());
	String bucket = ConfigPropertiesReader.getInstance()
			.getProperty(ConfigConstants.P_NAME_AWS_BUCKET_NAME);

	Path file = FileUtils.findFirstDeeperInDirByName(Paths.get("."), "other/Application.class");

	h.uploadFile(bucket, "pref", file.toFile());



}


@Test
void testUploadFileList() throws IOException, InterruptedException {

	AmazonFileTransferHelper h = new AmazonFileTransferHelper(s3ClientHolder.getAmazonS3());
	String bucket = ConfigPropertiesReader.getInstance()
			.getProperty(ConfigConstants.P_NAME_AWS_BUCKET_NAME);

	//List<File> fileList = FileUtils.findAllDeeperInDirByTail(Paths.get("."), "").map(p->p.toFile()).collect(Collectors.toList());

	List<File> list = FileUtils.listFilesInCurrentDirectory();
	MultipleFileUpload multipleFileUpload = h.uploadFileList(bucket, null, list);

	List<File> fileList =  FileUtils.findFilesInCurrentDirectoryAndSubDirectories(((path,a)->path.toString().endsWith(".properties")));

	MultipleFileUpload multipleFileUpload2 = h.uploadFileList(bucket, null, fileList);


}

/**
 *
 * @throws IOException
 * @throws InterruptedException
 */
@Test
void uploadTempFile() throws IOException, InterruptedException {

	AmazonFileTransferHelper h = new AmazonFileTransferHelper(s3ClientHolder.getAmazonS3());
	File file = null;
	try {
		file = MiscTestHelperUtils.createSampleFile("aws-test", ".txt", 20);

		ItemCollection<QueryOutcome> items = AWSInteractionHelper.queryWithFilePathGlobalIndex(tableName, file.toString(), dynamoDBClientHolder);
		AtomicInteger integerBefore = new AtomicInteger();
		int itemCount1 = dynamoDBClientHolder.getItemCount(tableName);

		Upload upload = h.uploadFile(bucketName, null, file);
		System.out.println(file);

		ItemCollection<QueryOutcome> items2 = AWSInteractionHelper.queryWithFilePathGlobalIndex(tableName, file.toString(), dynamoDBClientHolder);
		AtomicInteger integer= new AtomicInteger();

		int itemCount2 = dynamoDBClientHolder.getItemCount(tableName);

	} finally {
		if (file != null) {
	//		file.deleteOnExit();
		}

	}

}

/**
 *
 * @throws IOException
 * @throws InterruptedException
 */
void uploadTempFileToS3AndDelete() throws IOException, InterruptedException {

	AmazonFileTransferHelper h = new AmazonFileTransferHelper(s3ClientHolder.getAmazonS3());
	File file = null;
	try {
		file = MiscTestHelperUtils.createSampleFile("aws-test", ".txt", 20);

		Upload upload = h.uploadFile(bucketName, null, file);

		logger.info("File uploaded: {}", upload);

		assertTrue(s3ClientHolder.doesObjectExists(bucketName, file.toString()));

		s3ClientHolder.deleteObject(bucketName, file.toString());

		boolean b = s3ClientHolder.doesObjectExists(bucketName, file.toString());

		Assertions.assertFalse(b);
	} finally {
		if (file != null) {
			file.deleteOnExit();
		}

	}

}

@Test
void uploadBigFile() throws IOException, InterruptedException {

	AmazonFileTransferHelper h = new AmazonFileTransferHelper(s3ClientHolder.getAmazonS3());
	String bucket = ConfigPropertiesReader.getInstance()
			.getProperty(ConfigConstants.P_NAME_AWS_BUCKET_NAME);

	Path path = FileUtils.findFirstDeeperInDirByName(Paths.get("."), BIG_FILE_NAME);

	//Table table = dynamoDBClientHolder.getTable(tableName);

	ItemCollection<QueryOutcome> items = AWSInteractionHelper.queryWithFilePathGlobalIndex(tableName, path.toString(), dynamoDBClientHolder);
	AtomicInteger integerBefore = new AtomicInteger();

	items.forEach((i) -> integerBefore.incrementAndGet());

	//logger.info("Items count before: {}. Items: {}",integerBefore, items);

	h.uploadFile(bucket, null, path.toFile());

	ItemCollection<QueryOutcome> itemsAfter = AWSInteractionHelper.queryWithFilePathGlobalIndex(tableName, path.toString(), dynamoDBClientHolder);

	AtomicInteger integerAfter = new AtomicInteger();

	itemsAfter.forEach((i) -> integerAfter.incrementAndGet());
	//	logger.info("Items count after: {}. Items: {}",integerAfter, itemsAfter);

	System.out.println();

	List<Map<String, AttributeValue>> maps = dynamoDBClientHolder.scanTable(tableName);

	//dynamoDBClientHolder.deleteAllItemsInTable(tableName);

//	List<Map<String, AttributeValue>> mapAfter = dynamoDBClientHolder.scanTable(tableName);

	System.out.println();
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