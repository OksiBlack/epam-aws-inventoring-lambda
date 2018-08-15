package com.ebsco.platform.core.awsclientholders;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
import com.ebsco.platform.configuration.ConfigConstants;
import com.ebsco.platform.configuration.ConfigPropertiesReader;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class AmazonDynamoDBClientHolder implements IFaceAWSClientHolder {
public static final String FILE_TYPE = "fileType";
public static final String FILE_PATH = "filePath";
public static final String PACKAGE_ID = "packageId";
public static final String ORIGIN_TIME_STAMP = "originTimeStamp";
public static final String FILE_PATH_INDEX = "filePathIndex";
public static final String FILE_TYPE_INDEX = "fileTypeIndex";
private AmazonDynamoDB amazonDynamoDB;
public static final String SIZE = "fileSize";
public static final String META = "metadata";
public static final String ACTION = "actionName";
public static final String USER = "actionUser";

public static final Logger logger = LogManager.getLogger();

public AmazonDynamoDBClientHolder(AmazonDynamoDB amazonDynamoDB) {
	this.amazonDynamoDB = amazonDynamoDB;
}

public AmazonDynamoDBClientHolder(Regions regions) {
	this(AmazonDynamoDBClientBuilder.standard()
			.withRegion(regions)
			.build());
}

public AmazonDynamoDBClientHolder() {
	ConfigPropertiesReader reader = null;

	reader = ConfigPropertiesReader.getInstance();

	String reg = reader.getProperty(ConfigConstants.P_NAME_AWS_REGION, ConfigConstants.DEFAULT_REGION.getName());
	Regions regions = Regions.fromName(reg);
	amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
			.withRegion(regions)
			.build();
}

public AmazonDynamoDB getAmazonDynamoDB() {
	return amazonDynamoDB;
}

/**
 * @param tableName
 * @param keyName
 * @param keyValue
 */
public GetItemRequest getItem(String tableName, String keyName, String keyValue) {

	Map<String, AttributeValue> keyToGet =
			new HashMap<>();

	keyToGet.put(keyName, new AttributeValue(keyValue));

	GetItemRequest request = new GetItemRequest()
			.withKey(keyToGet)
			.withTableName(tableName);

	Map<String, AttributeValue> returnedItem =
			amazonDynamoDB.getItem(request)
					.getItem();
	if (returnedItem != null) {
		Set<String> keys = returnedItem.keySet();
		for (String key : keys) {
			logger.info("{}: {}\n",
					key, returnedItem.get(key));
		}
	} else {
		logger.info("No item found with the key {}!\n", keyValue);
	}

	return request;
}

/**
 *
 */
public void deleteAllTables() {
	List<String> tables = listTables().getTableNames();
	tables.forEach((t) -> amazonDynamoDB.deleteTable(t));

}

/**
 *
 */
public void deleteTables(String... tables) {
	Arrays.stream(tables)
			.forEach((t) -> amazonDynamoDB.deleteTable(t));
}

/**
 * @param tableName
 * @param keyName
 * @param keyValue
 * @param args
 */
public PutItemResult addNewItem(String tableName, String keyName, String keyValue, String[] args) {

	Map<String, AttributeValue> itemValues =
			new HashMap<>();
	itemValues.put(keyName, new AttributeValue(keyValue));

	List<String[]> extraFieldValuePairs = new ArrayList<String[]>();

	// any additional args (fields to add to database)?
	for (int x = 0; x < args.length; x++) {
		String[] fields = args[x].split("=", 2);
		if (fields.length == 2) {
			extraFieldValuePairs.add(fields);
		} else {
			logger.throwing(new IllegalArgumentException(String.format("Invalid argument: %s\n", args[x])));
		}
	}
	for (String[] field : extraFieldValuePairs) {
		itemValues.put(field[0], new AttributeValue(field[1]));
	}
	PutItemResult putItemResult = null;

	putItemResult = amazonDynamoDB.putItem(tableName, itemValues);

	return putItemResult;
}

/**
 * @param tableName
 * @return
 */
public CreateTableResult createTableForIngestionLambda(String tableName) {

	/*if (tableName == null) {
		throw new IllegalArgumentException("You must specify table name.");
	}
*/

	// Attribute definitions
	ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();

	attributeDefinitions.add(new AttributeDefinition(PACKAGE_ID, ScalarAttributeType.S));
	attributeDefinitions.add(new AttributeDefinition(ORIGIN_TIME_STAMP, ScalarAttributeType.N));
	attributeDefinitions.add(new AttributeDefinition().withAttributeName(FILE_PATH)
			.withAttributeType("S"));
	attributeDefinitions.add(new AttributeDefinition().withAttributeName(FILE_TYPE)
			.withAttributeType("S"));

	// Key schema for table
	List<KeySchemaElement> tableKeySchema = new ArrayList<>();
	tableKeySchema.add(new KeySchemaElement().withAttributeName(PACKAGE_ID)
			.withKeyType(KeyType.HASH)); // Partition key
	tableKeySchema.add(new KeySchemaElement().withAttributeName(ORIGIN_TIME_STAMP)
			.withKeyType(KeyType.RANGE)); // Sort key

	// Initial provisioned throughput settings for the indexes
	ProvisionedThroughput ptIndex = new ProvisionedThroughput().withReadCapacityUnits(1L)
			.withWriteCapacityUnits(1L);

	// Path Index
	GlobalSecondaryIndex filePathIndex = new GlobalSecondaryIndex().withIndexName(FILE_PATH_INDEX)
			.withProvisionedThroughput(ptIndex)
			.withKeySchema(new KeySchemaElement().withAttributeName(FILE_PATH)
					.withKeyType(KeyType.HASH)/* Partition
			key*/, new KeySchemaElement().withAttributeName(ORIGIN_TIME_STAMP)
					.withKeyType(KeyType.RANGE))
			.withProjection(new Projection().withProjectionType("KEYS_ONLY"));

	// fileTypeIndex
	GlobalSecondaryIndex fileTypeIndex = new GlobalSecondaryIndex().withIndexName(FILE_TYPE_INDEX)
			.withProvisionedThroughput(ptIndex)
			.withKeySchema(new KeySchemaElement().withAttributeName(FILE_TYPE)
					.withKeyType(KeyType.HASH)/* Partition
			key*/, new KeySchemaElement().withAttributeName(ORIGIN_TIME_STAMP)
					.withKeyType(KeyType.RANGE))
			.withProjection(new Projection().withProjectionType("KEYS_ONLY"));

	CreateTableRequest request = formCreateTableRequest(tableName, attributeDefinitions, tableKeySchema, filePathIndex, fileTypeIndex);

	CreateTableResult result = amazonDynamoDB.createTable(request);

	logger.info(result.getTableDescription()
			.getTableName());
	logger.info(result);
	logger.info("Done!");

	return result;
}

/**
 * @param tableName
 * @param attributeDefinitions
 * @param tableKeySchema
 * @param globalSecondaryIndexes
 * @return
 */
public CreateTableRequest formCreateTableRequest(String tableName, List<AttributeDefinition> attributeDefinitions, List<KeySchemaElement> tableKeySchema, GlobalSecondaryIndex... globalSecondaryIndexes) {
	return new CreateTableRequest().withTableName(tableName)
			.withProvisionedThroughput(
					new ProvisionedThroughput().withReadCapacityUnits((long) 1)
							.withWriteCapacityUnits((long) 1))
			.withAttributeDefinitions(attributeDefinitions)
			.withKeySchema(tableKeySchema)
			.withGlobalSecondaryIndexes(globalSecondaryIndexes);
}

/**
 * @return
 */
public ListTablesResult listTables() {
	ListTablesResult listTablesResult = amazonDynamoDB.listTables();
	//listTablesResult.getTableNames().forEach(System.out::println);
	return listTablesResult;
}

/**
 * @param tableName
 * @return
 */
public boolean createTableIfNotExists(String tableName) {

	List<String> tableNames = listTables().getTableNames();
	if (tableNames.contains(tableName)) {
		return true;
	} else {
		createTableForIngestionLambda(tableName);
		return true;
	}
}

/**
 * @param tableName
 * @return
 */
public boolean isTableExists(String tableName) {
	boolean res = false;

	List<String> tableNames = listTables().getTableNames();
	if (tableNames.contains(tableName)) {
		res = true;
	}

	return res;
}

/**
 * @param tableName
 * @return
 */
public Table getTable(String tableName) {
	DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);

	return dynamoDB.getTable(tableName);
}

@Override
public void shutdown() {
	amazonDynamoDB.shutdown();
}

/**
 * @param tName
 * @param indexName
 * @param keyConditionExpression
 * @param nameMap
 * @param valueMap
 * @return
 */
public ItemCollection<QueryOutcome> queryWithGlobalIndex(String tName, String indexName,
														 String keyConditionExpression, NameMap nameMap, ValueMap valueMap) {
	DynamoDB dynamoDB = new DynamoDB(this.getAmazonDynamoDB());
	Table table = dynamoDB.getTable(tName);

	Index index = table.getIndex(indexName);

	QuerySpec spec = new QuerySpec()
			.withKeyConditionExpression(keyConditionExpression);
	if (nameMap != null && nameMap.size() > 0) {
		spec.withNameMap(nameMap);
	}

	spec.withValueMap(valueMap);

	ItemCollection<QueryOutcome> items = index.query(spec);
	Iterator<Item> iter = items.iterator();
	while (iter.hasNext()) {
		logger.debug(iter.next()
				.toJSONPretty());
	}

	return items;
}

/**
 * @param table
 * @param keyName
 * @param itemPackId
 * @return
 */
public ItemCollection queryByPrimaryKey(Table table, String keyName, Object itemPackId) {
	QuerySpec querySpec = new QuerySpec()
			.withKeyConditionExpression(keyName + " = :v_id")
			.withValueMap(new ValueMap()
					.with(":v_id", itemPackId));

	ItemCollection<QueryOutcome> items = table.query(querySpec);
	for (Item item : items) {
		logger.trace(item.toJSONPretty());
	}
	return items;
}

/**
 * @param tName
 * @param projectionExpression
 * @param filterExspression
 * @param nameMap
 * @param valueMap
 * @return
 */
public ItemCollection<ScanOutcome> scanTable(String tName, String projectionExpression, String filterExspression, NameMap nameMap,
											 ValueMap valueMap) {//•

	DynamoDB dynamoDB = new DynamoDB(getAmazonDynamoDB());
	Table table = dynamoDB.getTable(tName);

	ScanSpec scanSpec = new ScanSpec().withProjectionExpression(projectionExpression)
			.withFilterExpression(filterExspression)
			.withNameMap(nameMap)
			.withValueMap(valueMap);
	ItemCollection<ScanOutcome> items = null;

	items = table.scan(scanSpec);
	Iterator<Item> iter = items.iterator();
	while (iter.hasNext()) {
		Item item = iter.next();
		logger.trace(item.toJSONPretty());
	}

	return items;

}



public PrimaryKey constructInventoringLambdaPrimaryKey(Map<String, AttributeValue> m){
	long rangeKeyValue = Long.parseLong(m
			.get(ORIGIN_TIME_STAMP)
			.getN());
	PrimaryKey key = new PrimaryKey(PACKAGE_ID, m
			.get(PACKAGE_ID)
			.getS(), ORIGIN_TIME_STAMP, rangeKeyValue);
	return key;
}
/**
 * @param tName
 */
public void deleteAllItemsInTable(String tName) {

	List<Map<String, AttributeValue>> list = scanTable(tName);
	deleteItemsFromTable(tName, list);

}

/**
 *
 * @param tableName
 * @param list
 */
public void deleteItemsFromTable(String tableName, List<Map<String, AttributeValue>> list) {

	DynamoDB dynamoDB = new DynamoDB(getAmazonDynamoDB());
	Table table = dynamoDB.getTable(tableName);

	list.forEach((m) -> {
		long rangeKeyValue2 = Long.parseLong(m
				.get(ORIGIN_TIME_STAMP)
				.getN());
		DeleteItemSpec deleteItemSpec = new DeleteItemSpec()

				.withPrimaryKey(PACKAGE_ID, m.get(PACKAGE_ID)
						.getS(), ORIGIN_TIME_STAMP, rangeKeyValue2);

		table.deleteItem(deleteItemSpec);

	});
}

/**
 * @param tName
 * @return
 */
public List<Map<String, AttributeValue>> scanTable(String tName) {//•

	ScanResult scanres = amazonDynamoDB.scan(new ScanRequest().withTableName(tName));

	return scanres.getItems();

}

public ScanResult getScanTableResult(String tName) {//•

	ScanResult scanres = amazonDynamoDB.scan(new ScanRequest().withTableName(tName));

	return scanres;

}

/**
 * @param tName
 * @return
 */
public int getItemCount(String tName) {//•
	return getScanTableResult(tName).getCount();

}

}
