/*
# Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

*/

package com.awstrainers.devcourse.sdkdemos;

import static java.text.MessageFormat.format;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ProgressEvent;
import com.amazonaws.services.s3.model.ProgressListener;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.StringInputStream;

public class S3Test {

	public static final Log log = LogFactory.getLog(S3Test.class);

	private static final Regions REGION = Regions.US_EAST_1;

	private static String bucketName;
	private static String objectKey;


	public static final AWSCredentials cred =
			new ClasspathPropertiesFileCredentialsProvider("AwsCredentials.properties").getCredentials();

	@BeforeClass
	public static void createTestBucket() throws Exception {
		bucketName = "s3testbucket" + UUID.randomUUID();
		objectKey = "s3TestObject" + UUID.randomUUID();
		AmazonS3 client = new AmazonS3Client(cred);
		client.createBucket(bucketName);
		InputStream in = new StringInputStream("Hello!");
		client.putObject(bucketName, objectKey, in, new ObjectMetadata());
		in.close();
	}

	@AfterClass
	public static void removeTestBucket() {
		AmazonS3 client = new AmazonS3Client(cred);
		ListObjectsRequest request = new ListObjectsRequest()
			.withBucketName(bucketName);
		ObjectListing objList = client.listObjects(request);
		for (S3ObjectSummary summary : objList.getObjectSummaries()) {
			client.deleteObject(bucketName, summary.getKey());
			log.debug(format("Cleanup: removing {0} key.", summary.getKey()));
		}
		log.debug(format("Cleanup: removing {0} bucket.", bucketName));
		client.deleteBucket(bucketName);
	}


	@Test
	public void checkListBuckets() {
		AmazonS3 client = new AmazonS3Client(cred);
		client.setRegion(Region.getRegion(REGION));
		List<Bucket> buckets = client.listBuckets();

		log.info(format("{0} buckets listed.", buckets.size()));
		for (Bucket bucket : client.listBuckets()) {
			log.debug(format("Bucket {0} owned by {1}.", format(bucket.getName(), bucket.getOwner().getDisplayName())));
		}

	}

	@Test
	public void checkListObjects() throws Exception {
		AmazonS3 client = new AmazonS3Client(cred);
		client.setRegion(Region.getRegion(REGION));

		ListObjectsRequest request = new ListObjectsRequest()
				.withBucketName(bucketName);

		ObjectListing objList = client.listObjects(request);

		for (S3ObjectSummary summary : objList.getObjectSummaries()) {
			log.debug(format("Object found under the {0} key.", summary.getKey()));
		}
	}

	@Test
	public void checkUploadObject() throws Exception {
		AmazonS3 client = new AmazonS3Client(cred);
		log.info(format("Using {0} bucket to post a new file.", bucketName));
		File file = createTempFile();
		PutObjectResult result = client.putObject(bucketName, file.getName(), file);
	}

	@Test
	public void checkUploadEncryptedObject() throws Exception {
		// Check the next url to read about how to use strong encryption with the jdk.
		// http://www.oracle.com/technetwork/es/java/javase/downloads/jce-7-download-432124.html
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(1024, new SecureRandom());
		KeyPair keyPair = keyPairGenerator.genKeyPair();
		// obviously that keypair should be stored somewhere, but this is just a test.
		EncryptionMaterials encryptionMaterials = new EncryptionMaterials(keyPair);
		AmazonS3 client = new AmazonS3EncryptionClient(cred, encryptionMaterials);
		log.info(format("Using {0} bucket to post a new encrypted file.", bucketName));
		File file = createTempFile();
		Bucket bucket = client.createBucket(bucketName);
		PutObjectResult result = client.putObject(bucket.getName(), file.getName(), file);
	}

	@Test
	public void checkTransferManager() throws Exception {
		AmazonS3 client = new AmazonS3Client(cred);

		TransferManager tx = new TransferManager(cred);
		File file = createTempFile();
		final Upload upload = tx.upload(bucketName, file.getName(), file);
		upload.addProgressListener(new ProgressListener() {
		    public void progressChanged(ProgressEvent progressEvent) {
		        log.info(format("{0}% uploaded.", upload.getProgress().getPercentTransferred()));
		        if (progressEvent.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
		        	log.info("Object uploaded.");
		        }
		    }
		});
		upload.waitForCompletion();
	}

	private File createTempFile() throws IOException {
		BufferedOutputStream out = null;
		try {
			File file = File.createTempFile("s3test" + UUID.randomUUID(), ".txt");

			byte[] zeroes = new byte[1024];
			out = new BufferedOutputStream(new FileOutputStream(file));
			for (int i=0; i < 100; i++) {
				out.write(zeroes);
			}
			return file;
		} finally {
			if (out != null) out.close();
		}
	}

}
