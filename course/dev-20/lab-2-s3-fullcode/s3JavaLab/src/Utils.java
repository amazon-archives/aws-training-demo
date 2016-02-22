// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

public class Utils {

    public static final String LAB_S3_BUCKET_NAME = "us-west-2-aws-training";
    public static final String LAB_S3_BUCKET_REGION = "us-west-2";
    public static final String[] LAB_BUCKET_DATA_FILE_KEYS = {
            "awsu-ilt/developing/v2.0/lab-2-s3/static/SampleInputFiles/DrugAdverseEvents_September.txt",
            "awsu-ilt/developing/v2.0/lab-2-s3/static/SampleInputFiles/DrugAdverseEvents_October.txt" };

    public static final String[] STUDENT_BUCKET_DATA_FILE_KEYS = { "DrugAdverseEvents_September.txt",
            "DrugAdverseEvents_October.txt" };

    public static Region getRegion() {
        Region region = Regions.getCurrentRegion();

        // This code is for local testing only.
        if (region == null) {
            region = Region.getRegion(Regions.US_WEST_1);
        }

        System.out.printf("Utils.getRegion() returned %s. %n ", region.getName());
        return region;
    }

    // Sets up the student's input bucket with sample data files retrieved from the lab bucket.
    public static void setup(AWSCredentials credentials, AmazonS3Client s3ForStudentBuckets) throws Exception {
        S3Object sampleDataObject = null;
        AmazonS3Client s3ForLabBucket = new AmazonS3Client(credentials);
        Region region = Region.getRegion(Regions.fromName(LAB_S3_BUCKET_REGION));
        s3ForLabBucket.setRegion(region);

        if (!s3ForStudentBuckets.doesBucketExist(DataTransformer.INPUT_BUCKET_NAME)) {
            s3ForStudentBuckets.createBucket(DataTransformer.INPUT_BUCKET_NAME);
        } else {
            DataTransformer.verifyBucketOwnership(DataTransformer.INPUT_BUCKET_NAME);
        }

        for (int i = 0; i < LAB_BUCKET_DATA_FILE_KEYS.length; i++) {
            sampleDataObject = s3ForLabBucket.getObject(LAB_S3_BUCKET_NAME, LAB_BUCKET_DATA_FILE_KEYS[i]);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(sampleDataObject.getObjectMetadata().getContentLength());

            s3ForStudentBuckets.putObject(DataTransformer.INPUT_BUCKET_NAME, STUDENT_BUCKET_DATA_FILE_KEYS[i],
                    sampleDataObject.getObjectContent(), objectMetadata);
        }
    }



}
