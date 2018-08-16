//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ebsco.platform.infrastructure.inventoringlambda;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3Entity;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.*;

public abstract class Application implements RequestHandler<S3Event, List<Item>> {
public static final String DYNAMODB_TABLE = "InventoryLambda_DYNAMODB_NAME";
public static final String ALLOWED_DIRECTORIES = "InventoryLambda_ALLOWED_DIRECTORIES";
public static final String LOG_ENABLED = "InventoryLambda_LOG_ENABLED";
public static final String ALL_DIRECTORIES = "all";
public static final String PATH_DELIMITER = "/";
public static final String EXT_DOT_SYMBOL = ".";
private AmazonS3 s3Client;
private DynamoDB dynamoDb;
private List<String> allowedDirectories;
private LambdaLogger logger;

public Application() {
	this.s3Client = AmazonS3ClientBuilder.standard()
			.build();
	this.dynamoDb = new DynamoDB((AmazonDynamoDBClientBuilder.standard()
			.withRegion(Regions.US_EAST_1)).build());
	this.getAllowedDirectories();
}

protected Application(AmazonS3 s3Client, DynamoDB dynamoDb, List<String> allowedDirectories) {
	this.s3Client = s3Client;
	this.dynamoDb = dynamoDb;
	this.allowedDirectories = allowedDirectories;
}

/**
 *
 * @param event
 * @param context
 * @return
 */
public List<Item> handleRequest(S3Event event, Context context) {
	this.setLogger(context);
	return this.storeEvent(event);
}

/**
 *
 * @param event
 * @return
 */
protected List<Item> storeEvent(S3Event event) {
	List<Item> items = new ArrayList();
	this.log("Storing events to dynamo...");

	try {
		Iterator var3 = event.getRecords()
				.iterator();

		while (var3.hasNext()) {
			S3EventNotificationRecord record = (S3EventNotificationRecord) var3.next();
			if (this.isDirectoryAllowed(record)) {
				Item item = this.createItem(record);
				this.log("Item created: " + item);
				items.add(item);
				this.getDbTable()
						.putItem(item);
				this.log("Item put to dynamo: " + item);
			}
		}
	}
	catch (Exception var6) {
		this.log(var6.getMessage());
	}

	return items;
}

/**
 *
 * @return
 */
protected abstract Table getDbTable();

/**
 *
 * @param var1
 * @return
 */
abstract Item createItem(S3EventNotificationRecord var1);

/**
 *
 * @param s3Entity
 * @return
 */
protected String getMetadata(S3Entity s3Entity) {
	try {
		ObjectMetadata objectMetadata = this.s3Client.getObjectMetadata(s3Entity.getBucket()
				.getName(), s3Entity.getObject()
				.getKey());
		return (new JSONObject(objectMetadata.getRawMetadata())).toString();
	}
	catch (Exception var3) {
		return null;
	}
}

/**
 *
 * @param s3Entity
 * @return
 */
protected String getKey(S3Entity s3Entity) {
	return s3Entity.getObject()
			.getKey();
}

/**
 *
 * @param record
 * @return
 */
protected String getEventName(S3EventNotificationRecord record) {
	return record.getEventName();
}

/**
 *
 * @param s3Entity
 * @return
 */
protected Long getSizeAsLong(S3Entity s3Entity) {
	return s3Entity.getObject()
			.getSizeAsLong();
}

/**
 *
 * @param record
 * @return
 */
protected String getUserId(S3EventNotificationRecord record) {
	return record.getUserIdentity()
			.getPrincipalId();
}

protected void log(String message) {
	if (this.logger != null && Boolean.getBoolean(System.getenv(LOG_ENABLED))) {
		this.logger.log(message + "\n");
	}

}

protected String getSubFolder(String path) {
	if(path==null||path.isEmpty()){
		return null;
	}
	if (path.contains(PATH_DELIMITER)) {
		String[] path_parts = path.split(PATH_DELIMITER);
		return path_parts[path_parts.length-1] ;
	} else {
		return path;
	}
}

protected String getFileName(String path) {
	if(path==null||path.isEmpty()){
		return null;
	}
	if (path.contains(PATH_DELIMITER)) {
		String[] path_parts = path.split(PATH_DELIMITER);
		return path_parts[path_parts.length-1] ;
	} else {
		return path;
	}
}

protected String getExtension(String fileName) {

	int i = fileName.lastIndexOf(EXT_DOT_SYMBOL);
	if(i!=-1 && i<fileName.length()-1){
		return fileName.substring(i+1);
	}else{
		return null;
	}
}

protected String getUuid(S3Entity s3Entity, DateTime dateTime) {
	String uuidSource = s3Entity.getObject() + s3Entity.getBucket()
			.getArn() + s3Entity + dateTime.toString();
	return UUID.nameUUIDFromBytes(uuidSource.getBytes())
			.toString();
}

/**
 *
 * @param context
 */
private void setLogger(Context context) {
	if (context != null) {
		this.logger = context.getLogger();
	}

}

private void getAllowedDirectories() {
	String var = System.getenv(ALLOWED_DIRECTORIES);
	if (var != null && !"all".equalsIgnoreCase(var)) {
		this.allowedDirectories = Arrays.asList(var.replace("\\s", "")
				.split(","));
	} else {
		this.allowedDirectories = Collections.singletonList(ALL_DIRECTORIES);
	}

}

private boolean isDirectoryAllowed(S3EventNotificationRecord record) {
	if (this.allowedDirectories.contains(ALL_DIRECTORIES)) {
		this.log("Directory allowed by default");
		return true;
	} else {
		String path = record.getS3()
				.getObject()
				.getKey();
		String key = path.split(PATH_DELIMITER)[0];
		boolean result = this.allowedDirectories.contains(key);
		if (!result) {
			this.log("Event [" + path + "] with key [" + key + "] is not among: " + this.allowedDirectories.toString());
		} else {
			this.log("Event allowed: [" + path + "], key [" + key + "], allowed folders: " + this.allowedDirectories.toString());
		}

		return result;
	}
}

public DynamoDB getDynamoDb() {
	return this.dynamoDb;
}
}
