// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

using Amazon;
using Amazon.S3;
using Amazon.S3.Model;
using System.IO;
using System.Linq;

namespace Lab2
{
    class Utils
    {
        public static string labS3BucketName = "us-west-2-aws-training";
        public static string labS3BucketRegion = "us-west-2";
        public static string[] labBucketDataFileKeys = 
        {
            "awsu-ilt/developing/v2.0/lab-2-s3/static/SampleInputFiles/DrugAdverseEvents_September.txt",
            "awsu-ilt/developing/v2.0/lab-2-s3/static/SampleInputFiles/DrugAdverseEvents_October.txt"
        };
        public static string[] StudentBucketDataFileKeys = 
        {
            "DrugAdverseEvents_September.txt",
            "DrugAdverseEvents_October.txt"
        };

         // Sets up the student's input bucket with sample data files retrieved from the lab bucket.
        public static void Setup(AmazonS3Client s3ForStudentBuckets)
        {
            RegionEndpoint region = RegionEndpoint.USWest2;
            AmazonS3Client s3ForLabBucket;
            string textContent = null;

            s3ForLabBucket = new AmazonS3Client(region);

            ListBucketsResponse responseBuckets = s3ForStudentBuckets.ListBuckets();

            foreach (S3Bucket bucket in responseBuckets.Buckets)
            {
                if (bucket.BucketName == DataTransformer.InputBucketName)
                {
                    DataTransformer.VerifyBucketOwnership(DataTransformer.InputBucketName);
                    break;
                }
                else
                {
                    DataTransformer.CreateBucket(DataTransformer.InputBucketName);
                }
            }

            for (int i = 0; i < labBucketDataFileKeys.Length; i++)
            {
                GetObjectRequest requestForStream = new GetObjectRequest
                {
                    BucketName = labS3BucketName,
                    Key = labBucketDataFileKeys[i]
                };

                using (GetObjectResponse responseForStream = s3ForLabBucket.GetObject(requestForStream))
                {
                    using (StreamReader reader = new StreamReader(responseForStream.ResponseStream))
                    {
                        textContent = reader.ReadToEnd();

                        PutObjectRequest putRequest = new PutObjectRequest
                        {
                            BucketName = DataTransformer.InputBucketName,
                            Key = labBucketDataFileKeys[i].ToString().Split('/').Last(),
                            ContentBody = textContent
                        };

                        putRequest.Metadata.Add("ContentLength", responseForStream.ContentLength.ToString());
                        s3ForStudentBuckets.PutObject(putRequest);
                    }                       
                }
            }
        }
    }    
}
