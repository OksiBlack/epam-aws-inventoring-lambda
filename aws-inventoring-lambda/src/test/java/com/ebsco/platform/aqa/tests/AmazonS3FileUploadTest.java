package com.ebsco.platform.aqa.tests;

import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.Upload;
import com.ebsco.platform.aqa.utils.AWSInteractionHelper;
import com.ebsco.platform.aqa.utils.MiscTestHelperUtils;
import com.ebsco.platform.configuration.ConfigConstants;
import com.ebsco.platform.core.helpers.AmazonFileTransferHelper;
import com.ebsco.platform.utils.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder.logger;
import static org.junit.jupiter.api.Assertions.*;

class AmazonS3FileUploadTest extends InventoringLambdaTest {

public static final String BIG_FILE_NAME = "DesktopImages-20180731T115600Z-001.zip";
public static final String SMALL_SIZE_FILE_NAME = "log4j2.xml";

//public static final String UPLOAD_FILE_NAME = reader.getProperty(ConfigConstants.P_NAME_FILE_NAME_TO_UPLOAD, ConfigConstants.DEFAULT_CONFIG_FILE_NAME);

@BeforeEach
void setUp() {
	logger.trace("AmazonS3FileUploadTest.setUp");

}

@DisplayName("Upload file to s3. Test lambda triggered")
@Test
void testLambdaTriggeredOnUploadFile() throws IOException, InterruptedException {

	AmazonFileTransferHelper h = new AmazonFileTransferHelper(s3ClientHolder.getAmazonS3());

	String fileName = reader.getProperty(ConfigConstants.P_NAME_FILE_NAME_TO_UPLOAD, SMALL_SIZE_FILE_NAME);

	Path file = FileUtils.findFirstDeeperInDirByName(Paths.get("."), fileName);

	h.uploadFile(bucketName, null, file.toFile());

}

@DisplayName("Upload file list to s3. Test lambda triggered")
@Test
void testLambdaTriggeredOnUploadFileList() throws IOException, InterruptedException {

	AmazonFileTransferHelper h = new AmazonFileTransferHelper(s3ClientHolder.getAmazonS3());

	List<File> fileList = FileUtils.findFilesInDirectoryAndSubDirectories(".", ((path, a) -> path.toString()
			.endsWith(".properties")));


	int itemCount1 = dynamoDBClientHolder.getItemCount(tableName);

	MultipleFileUpload multipleFileUpload2 = h.uploadFileList(bucketName, null, fileList);

	int itemCount2 = dynamoDBClientHolder.getItemCount(tableName);
	logger.info("Item count before: {}\nItem count after: {}\n", itemCount1, itemCount2);

	logger.info("Number of records in table should increase by number of files found.");

	assertTrue(itemCount2>itemCount1, "The number of inserted records should increase after inserting file list.");
}

/**
 * @throws IOException
 * @throws InterruptedException
 */

@DisplayName("Upload temporary file to s3. Test lambda triggered")
@Test
void testLambdaTriggeredOnFileUploadWithTempFile() throws IOException, InterruptedException {

	AmazonFileTransferHelper h = new AmazonFileTransferHelper(s3ClientHolder.getAmazonS3());
	File file = null;
	try {
		file = MiscTestHelperUtils.createSampleFile("aws-lambda-test-", ".txt", 10);

		ItemCollection<QueryOutcome> items = AWSInteractionHelper.queryWithFilePathGlobalIndex(tableName, file, dynamoDBClientHolder);
		logger.info("Retrieving items before upload");
		items.forEach((i) -> logger.trace(i.toJSONPretty()));
		int itemCount1 = dynamoDBClientHolder.getItemCount(tableName);

		Upload upload = h.uploadFile(bucketName, null, file);

		ItemCollection<QueryOutcome> items2 = AWSInteractionHelper.queryWithFilePathGlobalIndex(tableName, file, dynamoDBClientHolder);

		logger.info("Retrieving items after upload");
		items.forEach((i) -> logger.trace(i.toJSONPretty()));
		int itemCount2 = dynamoDBClientHolder.getItemCount(tableName);
		logger.info("Number of items in table: Before upload:  {}. After upload: {}.", itemCount1, itemCount2);
		assertEquals(itemCount1 + 1, itemCount2);
	} finally {
		if (file != null) {
			file.deleteOnExit();
		}

	}

}

/**
 * @throws IOException
 * @throws InterruptedException
 */

@DisplayName("Upload file to s3 and than delete it.Test lambda triggered both times ")
@Test
void testLambdaTriggeredOnFileUploadAndFileDelete() throws IOException, InterruptedException {

	AmazonFileTransferHelper h = new AmazonFileTransferHelper(s3ClientHolder.getAmazonS3());

	File file = null;
	try {
		file = MiscTestHelperUtils.createSampleFile("aws-test", ".txt", 20);
		int beforeUploadCount = dynamoDBClientHolder.getItemCount(tableName);

		Upload upload = h.uploadFile(bucketName, null, file);

		logger.info("File uploaded: {}", upload);
		int afterUploadCount = dynamoDBClientHolder.getItemCount(tableName);

		assertTrue(s3ClientHolder.doesObjectExists(bucketName, MiscTestHelperUtils.fileToObjectKey(file)), "Object should be uploaded");

		logger.info("Deleting object from s3: {}", upload);

		s3ClientHolder.deleteObject(bucketName, MiscTestHelperUtils.fileToObjectKey(file));

		assertFalse(s3ClientHolder.doesObjectExists(bucketName, MiscTestHelperUtils.fileToObjectKey(file)), "Object should be deleted");

		int afterDeleteCount = dynamoDBClientHolder.getItemCount(tableName);
		logger.info("Number of items in table: Before upload:  {}. After upload: {}. After delete: {}.", beforeUploadCount, afterUploadCount, afterDeleteCount);

		assertEquals(beforeUploadCount + 1, afterUploadCount, "Record should be inserted on file upload.");
		assertEquals(afterUploadCount + 1, afterDeleteCount, "Record should be inserted on object deletion.");

	} finally {
		if (file != null) {
			file.deleteOnExit();
		}

	}

}

@DisplayName("Upload big size file to s3. Test lambda triggered")
@Test
void testLambdaTriggeredOnBigFileUploadWithTempFile() throws IOException, InterruptedException {

	AmazonFileTransferHelper h = new AmazonFileTransferHelper(s3ClientHolder.getAmazonS3());
	Path path = FileUtils.findFirstDeeperInDirByName(Paths.get("."), BIG_FILE_NAME);

	ItemCollection<QueryOutcome> items = AWSInteractionHelper.queryWithFilePathGlobalIndex(tableName, path, dynamoDBClientHolder);
	logger.info("Retrieving items before upload");
	items.forEach((i) -> logger.trace(i.toJSONPretty()));

	int beforeCount = dynamoDBClientHolder.getItemCount(tableName);

	h.uploadFile(bucketName, null, path.toFile());
	logger.info("Retrieving items after upload");
	items.forEach((i) -> logger.trace(i.toJSONPretty()));
	ItemCollection<QueryOutcome> itemsAfter = AWSInteractionHelper.queryWithFilePathGlobalIndex(tableName, path, dynamoDBClientHolder);
	int afterCount = dynamoDBClientHolder.getItemCount(tableName);

	logger.info("Before: {}. After: {}", beforeCount, afterCount);

	assertEquals(beforeCount + 1, afterCount, "Number of overall records in table should increase by on after file upload.");

}


}