package com.ebsco.platform.aqa.utils;

import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder;
import com.ebsco.platform.core.helpers.AmazonFileTransferHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.ebsco.platform.core.awsclientholders.AmazonDynamoDBClientHolder.*;

public class AWSInteractionHelper {
public static final Logger logger = LogManager.getLogger();

public static void createAndUploadSampleFile(AmazonFileTransferHelper fileTransferHelper, int repeatNum,
											 String bucketName, String filePrefix,
											 String fileSuffix) throws IOException {
	File file1 = null;
	try {
		file1 = MiscTestHelperUtils.createSampleFile(filePrefix, fileSuffix, repeatNum);


		try {
			fileTransferHelper.uploadFile(bucketName, null, file1);
		}
		catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	} finally {
		if (file1 != null) {
			file1.deleteOnExit();
		}


	}
}

/**
 *
 * @param tName
 * @param path
 * @param dynamoDBClientHolder
 * @return
 * @throws IOException
 */
public static ItemCollection<QueryOutcome> queryWithFilePathGlobalIndex(String tName, Path path,
																		AmazonDynamoDBClientHolder dynamoDBClientHolder) throws IOException {
	return queryWithFilePathGlobalIndex(tName, path.toFile(), dynamoDBClientHolder);
}

/**
 * @param tName
 * @param path
 * @return
 */
public static ItemCollection<QueryOutcome> queryWithFilePathGlobalIndex(String tName, File path,
																		AmazonDynamoDBClientHolder dynamoDBClientHolder) throws IOException {

	NameMap nameMap = null;
	String fPath = ":fPath";
	String keyConditionExs = FILE_PATH + " = " + fPath;

	String pathKey = MiscTestHelperUtils.fileToObjectKey(path);
	ValueMap valueMap1 = new ValueMap();
	valueMap1.put(fPath, pathKey);


	return dynamoDBClientHolder.queryWithGlobalIndex(tName, FILE_PATH_INDEX, keyConditionExs, nameMap,
			valueMap1);
}

/**
 * @param tName
 * @param fileType
 * @param dynamoDBClientHolder
 * @return
 */
public static ItemCollection<QueryOutcome> queryWithFileTypeGlobalIndex(String tName, String fileType,
																		AmazonDynamoDBClientHolder dynamoDBClientHolder) {

	NameMap nameMap = null;
	String fType = ":fType";
	String keyConditionExs = FILE_TYPE + " = " + fType;

	ValueMap valueMap1 = new ValueMap();
	valueMap1.put(fType, fileType);


	return dynamoDBClientHolder.queryWithGlobalIndex(tName, FILE_TYPE_INDEX, keyConditionExs, nameMap,
			valueMap1);
}
}
