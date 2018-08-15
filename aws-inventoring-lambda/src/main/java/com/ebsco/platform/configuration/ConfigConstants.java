package com.ebsco.platform.configuration;

import com.amazonaws.regions.Regions;
import com.ebsco.platform.infrastructure.inventoringlambda.IngestionLambda;

public class ConfigConstants {
public static final String DEFAULT_CONFIG_FILE_NAME = "awsConfig.properties";

public static final String SYS_P_NAME_AWS_ID = "aws.accessKeyId";

public static final String SYS_P_NAME_CONFIG_FILE_NAME = "configFileName";

public static final String SYS_P_NAME_AWS_KEY = "aws.secretKey";

public static final String P_NAME_AWS_BUCKET_NAME = "aws.bucketName";

public static final String P_NAME_AWS_LAMBDA_FUNCTION_NAME = "aws.lambda.functionName";

public static final String P_NAME_AWS_UPLOADED_FILE_NAME="aws.uploadedFileName";

public static final String P_NAME_TABLE_NAME = "aws.tableName";
public static final String P_NAME_AWS_REGION = "aws.region";
public static final String P_NAME_AWS_LAMBDA_ROLE = "aws.lambda.role";
public static final String P_NAME_AWS_LAMBDA_HANDLER ="aws.lambda.handlerName";
public static final Regions DEFAULT_REGION = Regions.US_EAST_1;
public static final String DEFAULT_LAMBDA_ROLE="arn:aws:iam::501805042275:role/aws-lambda-full-access";

public static final String DEFAULT_LAMBDA_HANDLER = "com.ebsco.platform.infrastructure.inventoringlambda.IngestionLambda";

public static final String DEFAULT_DYNAMODB_TABLE_NAME = IngestionLambda.DEFAULT_DYNAMODB_TABLE_NAME;

public static final String DEFAULT_BUCKET_NAME = "us-east-1.bucket.155";
public static final String DEFAULT_UPLOAD_FILE_NAME = "log4j2.xml";

public static final String ZIP_EXTENSION = ".zip";

public static final String ZIP_NAME = "inventoringlambda-1.0-SNAPSHOT.zip";
}
