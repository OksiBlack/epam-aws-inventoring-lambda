# aws-inventoring-lambda module instructions #

1.App configuration depends on awsConfig.properties file included in resources.

----
Here you setup following properties

aws.bucketName=us.east.1.bucket.777
aws.uploadedFileName=1.txt
aws.tableName=NEW_TABLE_777
aws.region=US_EAST_1
aws.lambda.role=arn:aws:iam::501805042275:role/aws-lambda-full-access
aws.lambda.functionName=DynamoDBLambdaFunction777
aws.lambda.handlerName=com.ebsco.platform.infrastructure.inventoringlambda.IngestionLambda
---
2.Also it is assumed that log4j2.xml will be included in current directory.\ Also yoy can set systemProperty log4j.configurationFile specifying path to log4j2 configuration file.

gradle -Dlog4j.configurationFile="./log2j2.xml" runMain.

----
***Note:***
It is assumed that we are in root project directory, i.e. in directory aws-inventoring-lambda.


3. Please note that before executing actual tests some activity to prepare environment needed.

3.1 You need to build project with gradle so that .zip arthefact was created.

3.2 Than you need to execute com.ebsco.platform.core.AWSPreparationRunner.
You can just execute jar file with all dependencies or you can execute runMain task with gradle.
gradle.build file included.

3.3 Actual tests are in test sources.
com.ebsco.platform.aqa.tests.AmazonS3FileUploadTest
com.ebsco.platform.aqa.tests.AWSParametersTest 

