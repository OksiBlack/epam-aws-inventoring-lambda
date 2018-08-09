package com.ebsco.platform.infrastructure.core.awsclients;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.lambda.model.Runtime;
import com.ebsco.platform.infrastructure.configuration.ConfigConstants;
import com.ebsco.platform.infrastructure.configuration.PropertiesReader;
import com.ebsco.platform.infrastructure.utility.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AmazonLambdaClient implements IFaceAWSClient {

public static final int LAMBDA_MEMORY_SIZE = 2048;
public static final int LAMBDA_TIMEOUT = 20;
public static final String LAMBDA_INVOKE_FUNCTION = "lambda:InvokeFunction";
public static final String S_3_AMAZONAWS_COM = "s3.amazonaws.com";
private AWSLambda awsLambdaClient;

public AmazonLambdaClient(AWSLambda awsLambdaClient) {
	this.awsLambdaClient = awsLambdaClient;
}

public AmazonLambdaClient() {
	PropertiesReader reader = PropertiesReader.getInstance();

	String reg = reader.getProperty(ConfigConstants.P_NAME_AWS_REGION, ConfigConstants.DEFAULT_REGION.getName());
	Regions regions = Regions.fromName(reg);


	awsLambdaClient = AWSLambdaClientBuilder.standard()
			.withRegion(regions)
			.build();
}

public AmazonLambdaClient(Regions region) {
	this(AWSLambdaClientBuilder.standard()
			.withRegion(region)
			.build());

}

/**
 *
 * @param functionName
 */
public AddPermissionResult addPermissionForS3ToInvokeLambda(String functionName){
	return addPermission(functionName, S_3_AMAZONAWS_COM, LAMBDA_INVOKE_FUNCTION);
}

/**
 *
 * @param functionName
 * @param principal
 * @param action
 */
public AddPermissionResult addPermission(String functionName, String principal, String action){

	return awsLambdaClient.addPermission(new AddPermissionRequest().withFunctionName(functionName)
			.withPrincipal(principal)
			.withAction(action).withStatementId(functionName+ ZonedDateTime.now().toEpochSecond()));
}

/**
 * @param functionName
 * @param pathToZip
 * @param handler
 * @param role
 * @return
 * @throws IOException
 */
public CreateFunctionResult createFunction(String functionName, Path pathToZip, String handler, String role) throws IOException {
	CreateFunctionRequest request = new CreateFunctionRequest();
	request.withMemorySize(LAMBDA_MEMORY_SIZE);
	request.withTimeout(LAMBDA_TIMEOUT);
	FunctionCode code = new FunctionCode();
	code
			.withZipFile(FileUtils.bytesFromFileToByteBuffer(pathToZip));

	request.withFunctionName(functionName)
			.withRole(role)
			.withRuntime(Runtime.Java8)
			.withHandler(handler)
			.withCode(code);


/*	ByteBuffer buffer = new ByteB
code.setZipFile(ByteBuffer b)*/
	;
	return awsLambdaClient.createFunction(request);
}


public GetFunctionResult getFunction(String name) {
	return awsLambdaClient.getFunction(new GetFunctionRequest().withFunctionName(name));
}

public DeleteFunctionResult deleteFunction(String funcName) {
	return awsLambdaClient.deleteFunction(new DeleteFunctionRequest().withFunctionName(funcName));
}

public ListFunctionsResult listFunctions() {
	return awsLambdaClient.listFunctions();

}

public UpdateFunctionCodeResult updateFunctionCode(String name, Path pathToZip) throws IOException {
	return awsLambdaClient.updateFunctionCode(new UpdateFunctionCodeRequest().withFunctionName(name)
			.withZipFile(FileUtils.bytesFromFileToByteBuffer(pathToZip)));
}

public boolean isFunctionAlreadyExists(String name) {
	List<FunctionConfiguration> functions = listFunctions().getFunctions();
	long count = functions.stream()
			.filter((FunctionConfiguration f) -> f.getFunctionName()
					.equals(name))
			.count();
	return count > 0;
}

public InvokeResult invoke(String funcName) {
	return awsLambdaClient.invoke(new InvokeRequest().withFunctionName(funcName));

}


@Override
public void shutdown() {
	awsLambdaClient.shutdown();
}
}
