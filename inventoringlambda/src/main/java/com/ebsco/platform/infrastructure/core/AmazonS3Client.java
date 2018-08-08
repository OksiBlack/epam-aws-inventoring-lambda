package com.ebsco.platform.infrastructure.core;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.ebsco.platform.utility.Log4j2BasedLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

;

public class AmazonS3Client {
private AmazonS3 s3;
public static final Logger logger = LogManager.getLogger();

public AmazonS3Client() {
	s3 = AmazonS3ClientBuilder.standard()
			.withRegion(Regions.US_EAST_1)
			.build();


/*s3 = new com.amazonaws.services.s3.AmazonS3Client();
	Region usWest2 = Region.getRegion(Regions.US_WEST_2);
	s3.setRegion(usWest2);*/
}

/**
 * @param client
 */
public AmazonS3Client(AmazonS3 client) {
	this.s3 = client;
}

/**
 * @param bucketName
 * @return
 */
public Bucket createBucketIfNotExists(String bucketName) {
	Bucket b = null;
	if (s3.doesBucketExistV2(bucketName)) {
		System.out.format("Bucket %s already exists.\n", bucketName);
		b = getBucket(bucketName);
	} else {
		try {
			b = s3.createBucket(bucketName);
		} catch (AmazonS3Exception e) {
			System.err.println(e.getErrorMessage());
		}
	}
	return b;


}


/**
 * @param bucketName
 * @return
 */
public Bucket getBucket(String bucketName) {

	Bucket bucket = null;
	List<Bucket> buckets = s3.listBuckets();
	for (Bucket b : buckets) {
		if (b.getName().equals(bucketName)) {
			bucket = b;
		}
	}
	return bucket;
}

/**
 * @param bucketName
 * @param keyPrefix
 * @param filePath
 * @throws IOException
 */
public void uploadFile(String bucketName, String keyPrefix, Path filePath) throws IOException {

	Path path1 = filePath.toRealPath();

	String keyName = null;
	if (keyPrefix != null) {
		keyName = keyPrefix + '/' + filePath;
	} else {
		keyName = filePath.toString();
	}

	File f = path1.toFile();
	TransferManager transferManager = TransferManagerBuilder.standard().build();
	try {
		Upload xfer = transferManager.upload(bucketName, keyName, f);


		xfer.waitForCompletion();

	} catch (AmazonServiceException e) {
		logger.error(e.getErrorMessage(), e);
		System.exit(-1);
	} catch (InterruptedException e) {
		logger.error(e.getMessage(), e);
		System.exit(-1);

	}
	transferManager.shutdownNow();
}

/**
 * @param bucketName
 * @param key
 * @param path
 * @throws IOException
 */
public void putFileToBucket(String bucketName, String key, Path path) throws IOException {

	Path path1 = path.toRealPath();
	s3.putObject(new PutObjectRequest(bucketName, key, path1.toFile()));

}

/**
 * @param bucketName
 * @param key
 * @return * Download an object - When you download an object
 */
public S3Object downloadObjectFromBucket(String bucketName, String key) {

	Log4j2BasedLogger.getLogger().info("Downloading an object");
	S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
	Log4j2BasedLogger.getLogger().info("Content-Type: " + object.getObjectMetadata().getContentType());
	Log4j2BasedLogger.getLogger().info(object.getObjectContent());
	return object;
}


/**
 * @param bucketName
 * @param key
 * @return /*
 * * Delete an object - Unless versioning has been turned on for your bucket
 */

public void deleteObject(String bucketName, String key) {

	Log4j2BasedLogger.getLogger().info("Deleting an object\n");
	s3.deleteObject(bucketName, key);
}
//TODO verify
public void deleteObject(DeleteObjectRequest deleteObjectRequest, String bucketName, String key
) {
	Log4j2BasedLogger.getLogger().info("Deleting an object\n");
	s3.deleteObject(new DeleteObjectRequest(bucketName, key));
}

/**
 * @param bucketName
 */
public void deleteBucket(String bucketName) {

	Log4j2BasedLogger.getLogger().info("Deleting a bucket\n");
	s3.deleteBucket(bucketName);
}

/**
 * @param bucketName
 * @param keyName
 * @param filePath
 * @throws IOException
 */
public static void downloadFile(String bucketName, String keyName,
								Path filePath) throws IOException {
	logger.info("Downloading to file: " + filePath);
	Path path1 = filePath.toRealPath();

	File f = path1.toFile();
	TransferManager transferManager = TransferManagerBuilder.standard().build();
	try {
		Download xfer = transferManager.download(bucketName, keyName, f);


		xfer.waitForCompletion();
	} catch (AmazonServiceException e) {
		logger.error(e.getErrorMessage(), e);
		System.exit(1);
	} catch (InterruptedException e) {
		logger.error(e.getMessage(), e);
		System.exit(-1);

	}
	transferManager.shutdownNow();
}
}

