//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ebsco.platform.infrastructure.inventoringlambda;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification.S3Entity;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;

import java.util.List;

import org.joda.time.DateTime;

public class IngestionLambda extends Application {
public static final String DEFAULT_DYNAMODB_TABLE_NAME = "platform.infrastructure.dynamodb-catalog.s3rawdocument";
public static final String KEY = "packageId";
public static final String PATH = "filePath";
public static final String SIZE = "fileSize";
public static final String META = "metadata";
public static final String TIMESTAMP = "originTimeStamp";
public static final String TYPE = "fileType";
public static final String ACTION = "actionName";
private Table dbTable;

public IngestionLambda() {
	String tableName = System.getenv("InventoryLambda_DYNAMODB_NAME");
	if (tableName == null || tableName.isEmpty()) {
		tableName = "platform.infrastructure.dynamodb-catalog.s3rawdocument";
	}

	this.log("Using table name: " + tableName);
	this.dbTable = this.getDynamoDb().getTable(tableName);
}

protected IngestionLambda(AmazonS3 s3Client, DynamoDB dynamoDb, Table dbTable, List<String> allowedDirectories) {
	super(s3Client, dynamoDb, allowedDirectories);
	this.dbTable = dbTable;
}

protected Table getDbTable() {
	return this.dbTable;
}

protected Item createItem(S3EventNotificationRecord record) {
	S3Entity s3Entity = record.getS3();
	Item item = new Item();
	DateTime dateTime = record.getEventTime();
	String uuid = this.getUuid(s3Entity, dateTime);
	item.withPrimaryKey("packageId", uuid).withLong("originTimeStamp", dateTime.getMillis()).withString("filePath", this.getKey(s3Entity)).withLong("fileSize", this.getSizeAsLong(s3Entity)).withString("actionName", this.getEventName(record));
	String type = this.getSubFolder(s3Entity.getObject().getKey());
	if (type != null) {
		item.withString("fileType", type);
	}

	String metadata = this.getMetadata(s3Entity);
	if (metadata != null) {
		item.withJSON("metadata", metadata);
	}

	return item;
}
}
