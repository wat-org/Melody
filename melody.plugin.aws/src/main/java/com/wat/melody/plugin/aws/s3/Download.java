package com.wat.melody.plugin.aws.s3;

import java.io.File;

import com.amazonaws.services.s3.AmazonS3;
import com.wat.cloud.aws.s3.BucketName;
import com.wat.cloud.aws.s3.transfer.AwsS3DownloaderMultiThread;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.condition.Condition;
import com.wat.melody.api.annotation.condition.Conditions;
import com.wat.melody.api.annotation.condition.Match;
import com.wat.melody.common.transfer.exception.TransferException;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;
import com.wat.melody.plugin.aws.s3.common.Transfer;
import com.wat.melody.plugin.aws.s3.common.types.RemoteResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Conditions({ @Condition({ @Match(expression = "§[@provider]§", value = "aws.s3") }) })
public class Download extends Transfer {

	/**
	 * Task's name
	 */
	public static final String DOWNLOAD = "download";

	public Download() {
		super();
	}

	@Override
	public void doTransfer(AmazonS3 s3Connection, BucketName bucketName)
			throws TransferException, InterruptedException {
		new AwsS3DownloaderMultiThread(s3Connection, bucketName,
				getResourcesSpecifications(), getMaxPar(), this,
				Melody.getThreadFactory()).doTransfer();
	}

	@Override
	public ResourcesSpecification newResourcesSpecification(File basedir) {
		return new RemoteResourcesSpecification(basedir);
	}

}