package com.ebsco.platform.infrastructure.core;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class AmazonDynamoDBClient {
public static final String FILE_TYPE = "fileType";
public static final String FILE_PATH = "filePath";
public static final String PACKAGE_ID = "packageId";
public static final String ORIGIN_TIME_STAMP = "originTimeStamp";
private AmazonDynamoDB ddb;
public static final String KEY = "cdsKey";
public static final String SIZE = "fileSize";
public static final String META = "metadata";
public static final String ACTION = "actionName";
public static final String USER = "actionUser";


public static final Logger logger = LogManager.getLogger();

public AmazonDynamoDBClient(AmazonDynamoDB ddb) {
	this.ddb = ddb;
}

public AmazonDynamoDBClient() {
	this(AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1).build());
}

/**
 * @param tableName
 * @return
 */
public CreateTableResult createTable(String tableName) {

	if (tableName == null) {
		throw new IllegalArgumentException("You must specify table name.");
	}


	// Attribute definitions
	ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();


	attributeDefinitions.add(new AttributeDefinition(PACKAGE_ID, ScalarAttributeType.S));
	attributeDefinitions.add(new AttributeDefinition(ORIGIN_TIME_STAMP, ScalarAttributeType.N));
	attributeDefinitions.add(new AttributeDefinition().withAttributeName(FILE_PATH).withAttributeType("S"));
	attributeDefinitions.add(new AttributeDefinition().withAttributeName(FILE_TYPE).withAttributeType("S"));
	attributeDefinitions.add(new AttributeDefinition().withAttributeName(SIZE).withAttributeType("N"));
	attributeDefinitions.add(new AttributeDefinition().withAttributeName(ACTION).withAttributeType("S"));
	attributeDefinitions.add(new AttributeDefinition().withAttributeName(USER).withAttributeType("S"));
	attributeDefinitions.add(new AttributeDefinition().withAttributeName(META).withAttributeType("S"));
	// Key schema for table
	ArrayList<KeySchemaElement> tableKeySchema = new ArrayList<>();
	tableKeySchema.add(new KeySchemaElement().withAttributeName("packageId").withKeyType(KeyType.HASH)); // Partition key
	tableKeySchema.add(new KeySchemaElement().withAttributeName("originTimeStamp").withKeyType(KeyType.RANGE)); // Sort key

	// Initial provisioned throughput settings for the indexes
	ProvisionedThroughput ptIndex = new ProvisionedThroughput().withReadCapacityUnits(1L)
			.withWriteCapacityUnits(1L);

	// Path Index
	GlobalSecondaryIndex filePathIndex = new GlobalSecondaryIndex().withIndexName("filePathIndex")
			.withProvisionedThroughput(ptIndex)
			.withKeySchema(new KeySchemaElement().withAttributeName("filePath").withKeyType(KeyType.HASH)/* Partition key*/)
			.withProjection(new Projection().withProjectionType("KEYS_ONLY"));

	// fileTypeIndex
	GlobalSecondaryIndex fileTypeIndex = new GlobalSecondaryIndex().withIndexName("fileTypeIndex")
			.withProvisionedThroughput(ptIndex)
			.withKeySchema(new KeySchemaElement().withAttributeName("fileType").withKeyType(KeyType.HASH)/* Partition key*/)
			.withProjection(new Projection().withProjectionType("KEYS_ONLY"));


	CreateTableRequest request = new CreateTableRequest().withTableName(tableName)
			.withProvisionedThroughput(
					new ProvisionedThroughput().withReadCapacityUnits((long) 1).withWriteCapacityUnits((long) 1))
			.withAttributeDefinitions(attributeDefinitions).withKeySchema(tableKeySchema)
			.withGlobalSecondaryIndexes(filePathIndex, fileTypeIndex);

	CreateTableResult result = null;

	try {
		result = ddb.createTable(request);
		logger.info(result.getTableDescription().getTableName());


	} catch (AmazonServiceException e) {
		logger.error(e.getErrorMessage(), e);
		System.exit(-1);
	}
	logger.info("Done!");

	return result;
}



/**
 *
 * @return
 */
public ListTablesResult listTables() {
	ListTablesResult listTablesResult = ddb.listTables();
	listTablesResult.getTableNames().forEach(System.out::println);
	return listTablesResult;
}

/**
 *
 * @param tableName
 * @return
 */
public boolean createTableIfNotExists(String tableName){
	List<String> tableNames = listTables().getTableNames();
	if(tableNames.contains(tableName)){
		return false;
	}else{
		createTable(tableName);
		return true;
	}
}
}
