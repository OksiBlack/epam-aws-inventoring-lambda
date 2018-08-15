package com.ebsco.platform.core.awsclientholders;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.ebsco.platform.configuration.ConfigConstants;
import com.ebsco.platform.configuration.ConfigPropertiesReader;
import com.ebsco.platform.utils.Formatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;



public class AmazonS3ClientHolder implements IFaceAWSClientHolder {
public static final String S_3_OBJECT_CREATED_ALL = "s3:ObjectCreated:*";
private AmazonS3 amazonS3;

public AmazonS3 getAmazonS3() {
	return amazonS3;
}

public static final Logger logger = LogManager.getLogger();

public AmazonS3ClientHolder() {

	ConfigPropertiesReader reader = ConfigPropertiesReader.getInstance();

	String reg = reader.getProperty(ConfigConstants.P_NAME_AWS_REGION, ConfigConstants.DEFAULT_REGION.getName());
	Regions regions = Regions.fromName(reg);
	amazonS3 = AmazonS3ClientBuilder.standard()
			.withRegion(regions)
			.build();
}

/**
 * @param client
 */
public AmazonS3ClientHolder(AmazonS3 client) {
	this.amazonS3 = client;

}

/**
 *
 * @return
 */
public List<Bucket> listBuckets(){
	return amazonS3.listBuckets();


}

/**
 *
 * @param buck
 */
public void listObjects(String buck){
	amazonS3.listObjectsV2( buck);
}
/**
 *
 * @param regions
 */
public AmazonS3ClientHolder(Regions regions) {
	this(AmazonS3ClientBuilder.standard()
			.withRegion(regions)
			.build());
}

/**
 * @param bucketName
 * @return
 */
public Bucket createBucketIfNotExists(String bucketName) {
	Bucket b = null;
	if (amazonS3.doesBucketExistV2(bucketName)) {
		logger.info("Bucket {} already exists.\n", bucketName);
		b = getBucket(bucketName);
	} else {
		try {
			b = amazonS3.createBucket(bucketName);

		} catch (AmazonS3Exception e) {
			logger.error(e.getErrorMessage());
		}
	}
	return b;


}

public boolean doesBucketExists(String bucketName){
	return amazonS3.doesBucketExistV2(bucketName);

}
/**
 * @param bucketName
 * @return
 */
public Bucket createNewBucket(String bucketName) {
	return amazonS3.createBucket(bucketName);

}

/**
 *
 * @param bucketName
 * @param lambdaFuncArn
 */
public void addBucketNotificationConfiguration(String bucketName,
											   String lambdaFuncArn){

	BucketNotificationConfiguration configuration = new BucketNotificationConfiguration();
	configuration.addConfiguration(bucketName+System.currentTimeMillis(), new LambdaConfiguration(lambdaFuncArn,
			EnumSet.of(S3Event.ObjectCreated)));

	SetBucketNotificationConfigurationRequest request = new SetBucketNotificationConfigurationRequest(
			bucketName, configuration);
	amazonS3.setBucketNotificationConfiguration(request);

	//amazonS3.setBucketNotificationConfiguration(bucketName, configuration);

}
/**
 * @param bucketName
 * @return
 */
public Bucket getBucket(String bucketName) {

	Bucket bucket = null;
	List<Bucket> buckets = amazonS3.listBuckets();
	for (Bucket b : buckets) {
		if (b.getName().equals(bucketName)) {
			bucket = b;
		}
	}
	return bucket;
}

/**
 * @param bucketName
 * @param key
 * @param path
 * @throws IOException
 */
public void putFileObjectToBucket(String bucketName, String key, Path path) throws IOException {

	Path path1 = path.toRealPath();
	amazonS3.putObject(new PutObjectRequest(bucketName, key, path1.toFile()));

}

/**
 * @param bucketName
 * @param key
 * @return * Download an object - When you download an object
 */
public S3Object downloadObjectFromBucket(String bucketName, String key) {

	Formatter.getLogger().info("Downloading an object");
	S3Object object = amazonS3.getObject(new GetObjectRequest(bucketName, key));
	Formatter.getLogger().info("Content-Type: " + object.getObjectMetadata().getContentType());
	Formatter.getLogger().info(object.getObjectContent());
	return object;
}


/**
 * @param bucketName
 * @param key
 * @return /*
 * * Delete an object - Unless versioning has been turned on for your bucket
 */

public void deleteObject(String bucketName, String key) {

	Formatter.getLogger().info("Deleting an object\n");
	amazonS3.deleteObject(bucketName, key);
}
//TODO verify
public void deleteObject(DeleteObjectRequest deleteObjectRequest, String bucketName, String key
) {
	Formatter.getLogger().info("Deleting an object\n");
	amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
}

/**
 * @param bucketName
 */
public void deleteBucket(String bucketName) {

	Formatter.getLogger().info("Deleting a bucket\n");
	amazonS3.deleteBucket(bucketName);
}
public void deleteNotEmptyBucket(String bucketName, boolean isVersioned) {

	// Delete all objects from the bucket. This is sufficient
	// for unversioned buckets. For versioned buckets, when you attempt to delete objects, Amazon S3 inserts
	// delete markers for all objects, but doesn't delete the object versions.
	// To delete objects from versioned buckets, delete all of the object versions before deleting
	// the bucket (see below for an example).
	ObjectListing objectListing = amazonS3.listObjects(bucketName);
	while (true) {
		Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
		while (objIter.hasNext()) {
			amazonS3.deleteObject(bucketName, objIter.next().getKey());
		}

		// If the bucket contains many objects, the listObjects() call
		// might not return all of the objects in the first listing. Check to
		// see whether the listing was truncated. If so, retrieve the next page of objects
		// and delete them.
		if (objectListing.isTruncated()) {
			objectListing = amazonS3.listNextBatchOfObjects(objectListing);
		} else {
			break;
		}
	}

	if(isVersioned){
		// Delete all object versions (required for versioned buckets).
		VersionListing versionList = amazonS3.listVersions(new ListVersionsRequest().withBucketName(bucketName));
		while (true) {
			Iterator<S3VersionSummary> versionIter = versionList.getVersionSummaries().iterator();
			while (versionIter.hasNext()) {
				S3VersionSummary vs = versionIter.next();
				amazonS3.deleteVersion(bucketName, vs.getKey(), vs.getVersionId());
			}

			if (versionList.isTruncated()) {
				versionList = amazonS3.listNextBatchOfVersions(versionList);
			} else {
				break;
			}
		}
	}

	Formatter.getLogger().info("Deleting a bucket\n");
	amazonS3.deleteBucket(bucketName);
}



@Override
public void shutdown() {
	amazonS3.shutdown();
}
}

