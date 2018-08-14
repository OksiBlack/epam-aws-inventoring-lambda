package com.ebsco.platform.aqa.tests;

import com.amazonaws.regions.Regions;
import com.ebsco.platform.core.helpers.AmazonFileTransferHelper;
import com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder;
import com.ebsco.platform.core.awsclientholders.AmazonLambdaClientHolder;
import com.ebsco.platform.core.awsclientholders.AmazonS3ClientHolder;
import com.ebsco.platform.core.awsclientholders.IFaceAWSClientHolder;
import com.ebsco.platform.configuration.ConfigConstants;
import com.ebsco.platform.configuration.PropertiesReader;
import com.ebsco.platform.infrastructure.inventoringlambda.Application;
import com.ebsco.platform.utils.FileUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


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

}