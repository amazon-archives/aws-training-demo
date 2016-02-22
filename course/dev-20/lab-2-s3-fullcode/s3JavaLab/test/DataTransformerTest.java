// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import org.junit.Test;
import org.junit.AfterClass;
import org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.regions.Regions;
import com.amazonaws.regions.Region;

public class DataTransformerTest {

    @Test
    public void testAllFilesExistAndModifiedInOutput() throws Exception {
        DataTransformer.main(new String[0]);

        ObjectListing inputFileObjects = null;

        String fileKey = null;

        S3Object s3OutputObject = null;
        String outputFileStr = null;
        boolean isOutputFileModified = false;
        java.util.Scanner s = null;

        try {
            inputFileObjects = DataTransformer.s3ForStudentBuckets.listObjects(DataTransformer.INPUT_BUCKET_NAME);

            do {
                for (S3ObjectSummary objectSummary : inputFileObjects.getObjectSummaries()) {

                    fileKey = objectSummary.getKey();
                    s3OutputObject = DataTransformer.s3ForStudentBuckets
                            .getObject(new GetObjectRequest(DataTransformer.OUTPUT_BUCKET_NAME, fileKey));
                    s = new java.util.Scanner(s3OutputObject.getObjectContent()).useDelimiter("\\A");
                    outputFileStr = s.hasNext() ? s.next() : null;

                    if (outputFileStr != null && outputFileStr.contains(DataTransformer.JSON_COMMENT)) {
                        isOutputFileModified = true;
                    }

                    org.junit.Assert.assertTrue("Failure - Input file not transformed; output file does not contain JSON comment." + fileKey, isOutputFileModified);
                }
                inputFileObjects = DataTransformer.s3ForStudentBuckets.listNextBatchOfObjects(inputFileObjects);
            } while (inputFileObjects.isTruncated());

        } catch (AmazonServiceException ase) {
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Error Message: " + ace.getMessage());
        } finally {
            if (s != null) {
                s.close();
            }
        }

    }

 

}
