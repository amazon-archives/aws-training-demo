
// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.regions.Region;

// The DataTransformer class transforms objects in the input S3 bucket and puts the transformed objects into the output S3 bucket.
public class DataTransformer {

    // Before running the code, check that the ~/.aws/credentials file contains your credentials.

    public static final String[] ATTRS = { "genericDrugName", "adverseReaction" };

    // STUDENT TODO: Update input bucket name to a unique bucket name.
    public static final String INPUT_BUCKET_NAME = "input-someuniquename-123";

    // STUDENT TODO: Update output bucket name to a unique bucket name.
    public static final String OUTPUT_BUCKET_NAME = "output-someuniquename-123";

    public static final Region BUCKET_REGION = Utils.getRegion();

    public static final String JSON_COMMENT = "\"comment\": \"DataTransformer JSON\",";

    // The Amazon S3 client allows you to manage buckets and objects programmatically.
    public static AmazonS3Client s3ForStudentBuckets;

    // List used to store pre-signed URLs generated.
    public static List<URL> presignedUrls = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ObjectListing inputFileObjects = null;
        String fileKey = null;
        S3Object s3Object = null;
        File transformedFile = null;

        init();

        try {
            System.out.println("Transformer: Here we go...");
            createOutputBucketIfNeeded();

            inputFileObjects = s3ForStudentBuckets.listObjects(INPUT_BUCKET_NAME);

            do {
                for (S3ObjectSummary objectSummary : inputFileObjects.getObjectSummaries()) {
                    fileKey = objectSummary.getKey();
                    System.out.println("Transformer: Transforming file: " + fileKey);
                    if (fileKey.endsWith(".txt")) {
                        // STUDENT TODO: Retrieve each object from the input S3 bucket.
                        s3Object = s3ForStudentBuckets.getObject(new GetObjectRequest(INPUT_BUCKET_NAME, fileKey)); // @Del
                        // Transform object's content from CSV to JSON format.
                        transformedFile = transformText(s3Object); 
                        putObjectBasic(OUTPUT_BUCKET_NAME, fileKey, transformedFile);                        
                        // putObjectEnhanced(OUTPUT_BUCKET_NAME, fileKey, transformedFile);
                        generatePresignedUrl(OUTPUT_BUCKET_NAME, fileKey);
                    }
                }
                inputFileObjects = s3ForStudentBuckets.listNextBatchOfObjects(inputFileObjects);
            } while (inputFileObjects.isTruncated());

            printPresignedUrls();
            System.out.println("Transformer: DONE");
        } catch (AmazonServiceException ase) {
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    private static void init() throws Exception {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Transformer: Cannot load the credentials from the ~/.aws/credentials file.", e);
        }

        // STUDENT TODO: Create an instance of the AmazonS3Client object and set its region to the value of the BUCKET_REGION constant.
        s3ForStudentBuckets = new AmazonS3Client(credentials); // @Del
        s3ForStudentBuckets.setRegion(BUCKET_REGION);// @Del

        Utils.setup(credentials, s3ForStudentBuckets);
    }

    // Create the output bucket if it does not exist already.
    private static void createOutputBucketIfNeeded() throws Exception {
        if (!s3ForStudentBuckets.doesBucketExist(OUTPUT_BUCKET_NAME)) {
            System.out.println("Transformer: Creating output bucket: " + OUTPUT_BUCKET_NAME);
            s3ForStudentBuckets.createBucket(OUTPUT_BUCKET_NAME);
        } else {
            verifyBucketOwnership(OUTPUT_BUCKET_NAME);
        }
    }    

    // Reads the input stream of the S3 object. Transforms content to JSON format.
    // Return the transformed text in a File object.
    private static File transformText(S3Object s3Object) throws IOException {
        File transformedFile = new File("transformedfile.txt");
        String inputLine = null;
        StringBuffer outputStrBuf = new StringBuffer(1024);
        outputStrBuf.append("[\n");

        try (java.io.InputStream is = s3Object.getObjectContent();
                java.util.Scanner s = new java.util.Scanner(is);
                FileOutputStream fos = new FileOutputStream(transformedFile)) {
            s.useDelimiter("\n");
            while (s.hasNextLine()) {
                inputLine = s.nextLine();
                outputStrBuf.append(transformLineToJson(inputLine));
            }
            // Remove trailing comma at the end of the content. Close the array.
            outputStrBuf.deleteCharAt(outputStrBuf.length() - 2);
            outputStrBuf.append("]\n");
            fos.write(outputStrBuf.toString().getBytes());
            fos.flush();

        } catch (IOException e) {
            System.out.println("Transformer: Unable to create transformed file");
            e.printStackTrace();
        }

        return transformedFile;
    }

    private static String transformLineToJson(String inputLine) {
        String[] inputLineParts = inputLine.split(",");
        int len = inputLineParts.length;

        String jsonAttrText = "{\n  " + JSON_COMMENT + "\n";
        for (int i = 0; i < len; i++) {
            jsonAttrText = jsonAttrText + "  \"" + ATTRS[i] + "\"" + ":" + "\"" + inputLineParts[i] + "\"";
            if (i != len - 1) {
                jsonAttrText = jsonAttrText + ",\n";
            } else {
                jsonAttrText = jsonAttrText + "\n";
            }
        }
        jsonAttrText = jsonAttrText + "},\n";
        return jsonAttrText;
    }

    private static void putObjectBasic(String bucketName, String fileKey, File transformedFile) {
        // STUDENT TODO: Upload object to output bucket.        
        s3ForStudentBuckets.putObject(OUTPUT_BUCKET_NAME, fileKey, transformedFile); // @Del
    }
    
    private static void putObjectEnhanced(String bucketName, String fileKey, File transformedFile) {

        PutObjectRequest putRequest = new PutObjectRequest(bucketName, fileKey, transformedFile);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        // STUDENT TODO: Enable server side encryption.
        objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION); // @Del
        
        // STUDENT TODO: Add user metadata. "contact", "John Doe"
        objectMetadata.addUserMetadata("contact", "John Doe"); // @Del
        
        // STUDENT TODO: Upload object to output bucket.
        putRequest.setMetadata(objectMetadata); // @Del

        PutObjectResult response = s3ForStudentBuckets.putObject(putRequest); // @Del

        System.out.println("Transformer: Encryption status of uploaded object: " + response.getSSEAlgorithm()); // @Del
        System.out.println("Transformer: User metadata for name = 'contact': " + objectMetadata.getUserMetaDataOf("contact"));
    }

    private static void generatePresignedUrl(String bucketName, String objectKey) {
        URL url = null;

        java.util.Date expiration = new java.util.Date();
        long msec = expiration.getTime();
        msec += 1000 * 60 * 15; // 15 minutes
        expiration.setTime(msec);

        // STUDENT TODO: Generate a pre-signed URL to retrieve object (GET).
        GeneratePresignedUrlRequest generatePresignedUrlRequest = // @Del
        new GeneratePresignedUrlRequest(bucketName, objectKey); // @Del
        generatePresignedUrlRequest.setMethod(HttpMethod.GET); // @Del
        generatePresignedUrlRequest.setExpiration(expiration); // @Del

        url = s3ForStudentBuckets.generatePresignedUrl(generatePresignedUrlRequest); // @Del

        if (url != null) {
            presignedUrls.add(url);
        }
    }

    private static void printPresignedUrls() {
        System.out.println("Transformer: Pre-signed URLs: ");
        for (URL url : presignedUrls) {
            System.out.println(url + "\n");
        }
    }
    
    // Verify that this AWS account is the owner of the bucket.
    public static void verifyBucketOwnership(String bucketName) throws Exception {
        boolean ownedByYou = false;
        List<Bucket> buckets = s3ForStudentBuckets.listBuckets();
        for (Bucket bucket : buckets) {
            if (bucket.getName().equals(bucketName)) {
                ownedByYou = true;
                break;
            }
        }
        if (!ownedByYou) {
            String msg = String.format("The %s bucket is owned by another account. Specify a unique name for your bucket.", bucketName);
            throw new Exception(msg);
        }
    }

}
