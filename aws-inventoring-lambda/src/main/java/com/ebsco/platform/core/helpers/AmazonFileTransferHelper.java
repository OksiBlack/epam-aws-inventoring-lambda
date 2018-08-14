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
import java.util.List;

public class AmazonFileTransferHelper {
public static final Logger logger = LogManager.getLogger();

private AmazonS3 clientS3;

public AmazonFileTransferHelper() {
	this(AmazonS3ClientBuilder.standard()
			.build());
}

public AmazonFileTransferHelper(AmazonS3 amazonS3) {
	clientS3 = amazonS3;
}

/**
 * @param bucketName
 * @param keyPrefix  can be null
 * @param filePath
 * @throws IOException
 */
public void uploadFile(String bucketName, String keyPrefix, File filePath) throws InterruptedException, AmazonServiceException, IOException {

	if (filePath == null) {
		throw new IllegalArgumentException("filePath is null");
	}
	String keyName = null;
	if (keyPrefix != null) {
		keyName = keyPrefix + '/' + filePath;
	} else {
		keyName = filePath.toString();
	}

	File f = filePath.getCanonicalFile();

	TransferManager transferManager = TransferManagerBuilder.standard()
			.withS3Client(clientS3)
			.build();

	Upload xfer = transferManager.upload(bucketName, keyName, f);

	pollTransferProgress(xfer);

	xfer.waitForCompletion();

	transferManager.shutdownNow(false);
}

/**
 * @param bucketName
 * @param keyPrefix
 * @param filePaths
 * @throws IOException
 */
public void uploadFileList(String bucketName, String keyPrefix, List<File> filePaths) throws
		InterruptedException, AmazonServiceException {

	TransferManager transferManager = TransferManagerBuilder.standard()
			.withS3Client(clientS3)
			.build();

	MultipleFileUpload xfer = transferManager.uploadFileList(bucketName,
			keyPrefix, new File("."), filePaths);
	pollTransferProgress(xfer);

	xfer.waitForCompletion();

	transferManager.shutdownNow(false);
}

/**
 * @param bucketName
 * @param keyPrefix
 * @param directory
 * @param includeSubdirectories
 */
public void uploadDirectory(String bucketName, String keyPrefix, File directory, boolean includeSubdirectories) throws InterruptedException {


	TransferManager transferManager = TransferManagerBuilder.standard()
			.withS3Client(clientS3)
			.build();
	MultipleFileUpload xfer = transferManager.uploadDirectory(bucketName,
			keyPrefix, directory, includeSubdirectories);

	pollTransferProgress(xfer);

	xfer.waitForCompletion();
	transferManager.shutdownNow(false);


}

/**
 * @param bucketName
 * @param keyName
 * @param filePath
 * @throws IOException
 */
public void downloadFile(String bucketName, String keyName,
						 File filePath) throws IOException, InterruptedException {
	AmazonS3ClientHolder.logger.info("Downloading to file: " + filePath);

	File f = filePath.getCanonicalFile();
	TransferManager transferManager = TransferManagerBuilder.standard()
			.withS3Client(clientS3)
			.build();
	Download xfer = transferManager.download(bucketName, keyName, f);
	pollTransferProgress(xfer);

	xfer.waitForCompletion();

	transferManager.shutdownNow(false);
}

/**
 * @param bucketName
 * @param keyName
 * @param dirPath
 * @throws IOException
 * @throws InterruptedException
 */
public void downloadDirectory(String bucketName, String keyName,
							  File dirPath) throws IOException, InterruptedException {
	AmazonS3ClientHolder.logger.info("Downloading to file: " + dirPath);

	File f = dirPath.getCanonicalFile();
	TransferManager transferManager = TransferManagerBuilder.standard()
			.withS3Client(clientS3)
			.build();
	MultipleFileDownload xfer = transferManager.downloadDirectory(bucketName, keyName, f);


	pollTransferProgress(xfer);


	xfer.waitForCompletion();

	transferManager.shutdownNow(false);
}

/**
 *
 * @param transfer
 */

public void pollTransferProgress(Transfer transfer) {



		do {
			try {
				Thread.sleep(300);
			}
			catch (InterruptedException e) {
				return;
			}
			TransferProgress progress = transfer.getProgress();
			long transferred = progress.getBytesTransferred();
			long total = progress.getTotalBytesToTransfer();
			double pct = progress.getPercentTransferred();
			logger.info("Transferred: {}. Total: {}. Percent: {}", transferred, total, pct);
		} while (transfer.isDone() == false);

		Transfer.TransferState transferState = transfer.getState();
		logger.info("Final state: {}.", transferState);

}

/**
 * @param transfer
 */
public void pollTransferProgressWithListener(Transfer transfer) {


	transfer.addProgressListener((ProgressEvent progressEvent )->{
		long transferred = progressEvent.getBytesTransferred();
		long total = progressEvent.getBytes();
		double pct = transferred/(double)total*100;
		logger.info("Transferred: {}. Total: {}. Percent: {}", transferred, total, pct);

	});
}



}
