// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.regions.Region;

// The ReadySetGo class lists the number of buckets in your account.
public class ReadySetGo {

    // Before running the code, check that the ~/.aws/credentials file contains your credentials.

    static AmazonS3 s3;
    public static final Region BUCKET_REGION = Utils.getRegion();

    private static void init() throws Exception {

        // The ProfileCredentialsProvider will return your default credential profile by reading from the ~/.aws/credentials file.

        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new Exception("Cannot load AWS credentials.", e);
        }

        s3 = new AmazonS3Client(credentials);
        s3.setRegion(BUCKET_REGION);
    }

    public static void main(String[] args) throws Exception {

        System.out.println("============================================");
        System.out.println("Welcome to the AWS Java SDK! Ready, Set, Go!");
        System.out.println("============================================");

        init();

        // The Amazon S3 client allows you to manage buckets and objects programmatically.
        try {
            List<Bucket> buckets = s3.listBuckets();
            System.out.println("You have " + buckets.size() + " Amazon S3 bucket(s)");
        } catch (AmazonServiceException ase) {
            // AmazonServiceException represents an error response from an AWS service.
            // AWS service received the request but either found it invalid or encountered an error trying to execute it.
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            // AmazonClientException represents an error that occurred inside the client on the local host,
            // either while trying to send the request to AWS or interpret the response.
            // For example, if no network connection is available, the client won't be able to connect to AWS to execute a request and will throw an AmazonClientException.
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
}
