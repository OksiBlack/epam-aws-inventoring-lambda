package com.ebsco.platform.core.helpers;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.*;
import com.ebsco.platform.core.awsclientholders.AmazonS3ClientHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.List;

public class AmazonFileTransferHelper {
public static final Logger logger = LogManager.getLogger();
public static final int MILLIS_SLEEP = 1200;

private AmazonS3 s3Client;

public AmazonFileTransferHelper() {
	this(AmazonS3ClientBuilder.standard()
			.build());
}

public AmazonFileTransferHelper(AmazonS3 amazonS3) {
	s3Client = amazonS3;
}

public void shutdownClient(){

	if(s3Client !=null){
		s3Client.shutdown();
	}
}

public AmazonS3 getS3Client() {
	return s3Client;
}

/**
 * @param bucketName
 * @param keyPrefix  can be null
 * @param filePath
 * @throws IOException
 */
public Upload uploadFile(String bucketName, String keyPrefix, File filePath) throws InterruptedException, AmazonServiceException, IOException {

	if (filePath == null) {
		throw new IllegalArgumentException("filePath is null");
	}

	String canonicalPath = filePath.getCanonicalPath();
	String path = canonicalPath
			.startsWith("/") ? canonicalPath.substring(1) : canonicalPath;//handle unix root paths

	String keyName = null;
	if (keyPrefix != null) {
		keyName = keyPrefix + File.separator + path;

	} else {
		keyName = path;
	}

	TransferManager transferManager = TransferManagerBuilder.standard()
			.withS3Client(s3Client)
			.build();

	Upload xfer = transferManager.upload(bucketName, keyName, filePath);

	pollTransferProgress(xfer, MILLIS_SLEEP);

	xfer.waitForCompletion();

	transferManager.shutdownNow(false);
	return xfer;
}

/**
 *
 * @param bucketName
 * @param keyPrefix
 * @param filePath
 * @return
 * @throws InterruptedException
 * @throws AmazonServiceException
 * @throws IOException
 */
public Upload uploadFile(String bucketName, String keyPrefix, Path filePath) throws InterruptedException, AmazonServiceException, IOException {

	return uploadFile(bucketName, keyPrefix, filePath.toFile());
}
/**
 * @param bucketName
 * @param keyPrefix
 * @param filePaths
 * @throws IOException
 */
public MultipleFileUpload uploadFileList(String bucketName, String keyPrefix, List<File> filePaths) throws
		InterruptedException, AmazonServiceException, IOException {

	TransferManager transferManager = TransferManagerBuilder.standard()
			.withS3Client(s3Client)
			.build();

	MultipleFileUpload xfer = transferManager.uploadFileList(bucketName,
			keyPrefix, new File("."), filePaths);
	pollTransferProgress(xfer, MILLIS_SLEEP);

	xfer.waitForCompletion();

	transferManager.shutdownNow(false);
	return xfer;
}

/**
 * @param bucketName
 * @param keyPrefix
 * @param directory
 * @param includeSubdirectories
 */
public MultipleFileUpload uploadDirectory(String bucketName, String keyPrefix, File directory, boolean includeSubdirectories) throws InterruptedException, IOException {

	TransferManager transferManager = TransferManagerBuilder.standard()
			.withS3Client(s3Client)
			.build();


	MultipleFileUpload xfer = transferManager.uploadDirectory(bucketName,
			keyPrefix, directory, includeSubdirectories);

	pollTransferProgress(xfer, MILLIS_SLEEP);

	xfer.waitForCompletion();
	transferManager.shutdownNow(false);
	return xfer;

}

public MultipleFileUpload uploadDirectory(String bucketName, String keyPrefix, Path directory, boolean includeSubdirectories) throws InterruptedException, IOException {

	return uploadDirectory(bucketName, keyPrefix, directory.toFile(), includeSubdirectories);

}
/**
 * @param bucketName
 * @param keyName
 * @param filePath
 * @throws IOException
 */
public Download downloadFile(String bucketName, String keyName,
							 File filePath) throws IOException, InterruptedException {
	AmazonS3ClientHolder.logger.info("Downloading to file: " + filePath);

	File f = filePath.getCanonicalFile();
	TransferManager transferManager = TransferManagerBuilder.standard()
			.withS3Client(s3Client)
			.build();
	Download xfer = transferManager.download(bucketName, keyName, f);
	pollTransferProgress(xfer, MILLIS_SLEEP);

	xfer.waitForCompletion();

	transferManager.shutdownNow(false);
	return xfer;
}

/**
 * @param bucketName
 * @param keyName
 * @param dirPath
 * @throws IOException
 * @throws InterruptedException
 */
public MultipleFileDownload downloadDirectory(String bucketName, String keyName,
											  File dirPath) throws IOException, InterruptedException {
	AmazonS3ClientHolder.logger.info("Downloading to file: " + dirPath);

	File f = dirPath.getCanonicalFile();
	TransferManager transferManager = TransferManagerBuilder.standard()
			.withS3Client(s3Client)
			.build();
	MultipleFileDownload xfer = transferManager.downloadDirectory(bucketName, keyName, f);

	pollTransferProgress(xfer, MILLIS_SLEEP);

	xfer.waitForCompletion();

	transferManager.shutdownNow(false);
	return xfer;
}

/**
 * @param transfer
 */

public void pollTransferProgress(Transfer transfer, int millisSleep) {

	do {
		try {
			Thread.sleep(millisSleep);
		}
		catch (InterruptedException e) {
			return;
		}
		TransferProgress progress = transfer.getProgress();
		long transferred = progress.getBytesTransferred();
		long total = progress.getTotalBytesToTransfer();
		double pct = progress.getPercentTransferred();
		NumberFormat numberInstance = NumberFormat.getNumberInstance();
		numberInstance.setMaximumFractionDigits(2);
		numberInstance.setMinimumFractionDigits(2);

		logger.info("Transferred: {}. Total: {}. Percent: {}%", transferred, total, numberInstance
				.format(pct));
	} while (transfer.isDone() == false);

	Transfer.TransferState transferState = transfer.getState();
	logger.info("Final state: {}.", transferState);

}

/**
 * @param transfer
 */
public void pollTransferProgressWithListener(Transfer transfer) {

	NumberFormat numberInstance = NumberFormat.getNumberInstance();
	numberInstance.setMaximumFractionDigits(2);
	numberInstance.setMinimumFractionDigits(2);

	transfer.addProgressListener((ProgressEvent progressEvent) -> {
		long transferred = progressEvent.getBytesTransferred();
		long total = progressEvent.getBytes();
		double pct = transferred / (double) total * 100;
		logger.info("Transferred: {}. Total: {}. Percent: {}%", transferred, total, pct);

	});
}

}
