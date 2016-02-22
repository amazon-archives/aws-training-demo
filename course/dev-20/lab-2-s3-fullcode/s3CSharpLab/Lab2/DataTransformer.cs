// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

using Amazon.Runtime;
using Amazon.S3;
using Amazon.S3.Model;
using System;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO;
using System.Text;

namespace Lab2
{
    // The DataTransformer class transforms objects in the input S3 bucket and puts the transformed objects into the output S3 bucket.
    public static class DataTransformer
    {
        public static readonly string[] Attributes = { "genericDrugName", "adverseReaction" };

        // STUDENT TODO: Update input bucket name to a unique bucket name.
        public static readonly string InputBucketName = "input-someuniquename-123";

        // STUDENT TODO: Update output bucket name to a unique bucket name.
        public static readonly string OutputBucketName = "output-someuniquename-123";

        public static readonly string JsonComment = "\"comment\": \"DataTransformer JSON\",";

        // The Amazon S3 client allows you to manage buckets and objects programmatically.
        public static AmazonS3Client s3ForStudentBuckets;

        // List used to store pre-signed URLs generated.
        public static Collection<string> preSignedUrls = new Collection<string>();

        public static void Main()
        {
            ListObjectsRequest inputFileObjects;
            string fileKey = null;
            string transformedFile = null;

            Init();

            try
            {
                Debug.WriteLine("Transformer: Here we go...");
                CreateBucket(InputBucketName);
                CreateBucket(OutputBucketName);

                inputFileObjects = new ListObjectsRequest
                {
                    BucketName = InputBucketName
                };

                ListObjectsResponse listResponse;
                do // @Del
                { // @Del
                    // Get a list of objects
                    listResponse = s3ForStudentBuckets.ListObjects(inputFileObjects); // @Del
                    foreach (S3Object obj in listResponse.S3Objects) // @Del
                    { // @Del
                        fileKey = obj.Key; // @Del
                        Debug.WriteLine("Transformer: Transforming file: " + fileKey); // @Del
                        if (fileKey.EndsWith(".txt")) // @Del
                        { // @Del
                            // STUDENT TODO: Retrieve each object from the input S3 bucket.
                            // STUDENT TODO: Transform object's content. Invoke the transformText method provided in this file.
                            // STUDENT TODO: Upload object to output bucket.
                            transformedFile = TransformText(InputBucketName, fileKey); // @Del
                            
                            PutObjectRequest putRequest = new PutObjectRequest  // @Del
                            { // @Del
                                BucketName = OutputBucketName, // @Del
                                Key = fileKey, // @Del
                                ContentBody = transformedFile // @Del
                            }; // @Del
                            s3ForStudentBuckets.PutObject(putRequest); // @Del

                           // PutObjectEnhanced(outputBucketName, fileKey, transformedFile);
                            generatePresignedURL(fileKey);
                        }// @Del
                    }// @Del

                    // Set the marker property
                    inputFileObjects.Marker = listResponse.NextMarker; // @Del
                } while (listResponse.IsTruncated); // @Del

                PrintPresignedUrls();
                Debug.WriteLine("Transformer: DONE");
            }
            catch (AmazonServiceException ase)
            {
                Debug.WriteLine("Error Message:    " + ase.Message);
                Debug.WriteLine("HTTP Status Code: " + ase.StatusCode);
                Debug.WriteLine("AWS Error Code:   " + ase.ErrorCode);
                Debug.WriteLine("Error Type:       " + ase.ErrorType);
                Debug.WriteLine("Request ID:       " + ase.RequestId);
            }
            catch (AmazonClientException ace)
            {
                Debug.WriteLine("Error Message: " + ace.Message);
            }
        }

        private static void PrintPresignedUrls()
        {
            Debug.WriteLine("Transformer: Pre-signed URLs: ");
            foreach (string url in preSignedUrls)
            {
                Debug.WriteLine(url + "\n");
            }
        }

        //Create the output bucket if it does not exist already.
        public static void CreateBucket(string bucket)
        {
            ListBucketsResponse responseBuckets = s3ForStudentBuckets.ListBuckets();
            bool found = false;

            foreach (S3Bucket s3Bucket in responseBuckets.Buckets)
            {
                if (s3Bucket.BucketName == bucket)
                {
                    found = true;
                    VerifyBucketOwnership(bucket);
                    break;
                }
                else
                {
                    found = false;
                }
            }

            if (found == false)
            {
                Debug.Write("Transformer: Creating output bucket: " + bucket);
                PutBucketRequest request = new PutBucketRequest();
                request.BucketName = bucket;
                s3ForStudentBuckets.PutBucket(request);
            }
        }

        // Verify that this AWS account is the owner of the bucket.
        public static void VerifyBucketOwnership(string bucketName)
        {
            bool ownedByYou = false;

            ListBucketsResponse responseBuckets = s3ForStudentBuckets.ListBuckets();

            foreach (S3Bucket bucket in responseBuckets.Buckets)
            {
                if (bucket.BucketName.Equals(bucketName))
                {
                    ownedByYou = true;
                }
            }

            if (!ownedByYou)
            {
                Debug.WriteLine("The {0} bucket is owned by another account. Specify a unique name for your bucket. " +  bucketName);
            }

        }

        private static void generatePresignedURL(string objectKey)
        {
            string url = null;

            // STUDENT TODO: Generate a pre-signed URL to retrieve object (GET).
            GetPreSignedUrlRequest request = new GetPreSignedUrlRequest // @Del
            { // @Del
                BucketName = OutputBucketName, // @Del
                Key = objectKey, // @Del
                Protocol = Protocol.HTTP, // @Del
                Verb = HttpVerb.GET, // @Del
                Expires = DateTime.Now.AddSeconds(900), // 15 minutes  // @Del
            }; // @Del

            url = s3ForStudentBuckets.GetPreSignedURL(request); // @Del

            if (url != null)
            {
                preSignedUrls.Add(url);
            }

        }

        private static void PutObjectEnhanced(string bucketName, string fileKey, string transformedFile)
        {
            // STUDENT TODO: Enable server side encryption.
            // STUDENT TODO: Add user metadata. "contact", "John Doe"
            // STUDENT TODO: Upload object to output bucket.

            PutObjectRequest putRequest = new PutObjectRequest // @Del
            { // @Del
                BucketName = bucketName, // @Del
                Key = fileKey, // @Del
                ContentBody = transformedFile, // @Del
                ServerSideEncryptionMethod = ServerSideEncryptionMethod.AES256 // @Del
            }; // @Del

            putRequest.Metadata.Add("contact", "John Doe"); // @Del
            s3ForStudentBuckets.PutObject(putRequest); // @Del

            GetObjectMetadataRequest encryptionRequest = new GetObjectMetadataRequest() // @Del
            { // @Del
                BucketName = bucketName, // @Del
                Key = fileKey // @Del
            }; // @Del
            ServerSideEncryptionMethod objectEncryption = s3ForStudentBuckets.GetObjectMetadata(encryptionRequest).ServerSideEncryptionMethod; // @Del
            GetObjectMetadataResponse metadataResponse = s3ForStudentBuckets.GetObjectMetadata(encryptionRequest); // @Del
            string contactName = metadataResponse.Metadata["x-amz-meta-contact"]; // @Del

            //Debug.WriteLine("Transformer: Encryption status of uploaded object: " + objectEncryption.Value.ToString()); 
            //Debug.WriteLine("Transformer: User metadata for name = 'contact': " + contactName);
        }

        private static void Init()
        {
            // STUDENT TODO: Create an instance of the AmazonS3Client object.
            s3ForStudentBuckets = new AmazonS3Client(); //@Del
            Utils.Setup(s3ForStudentBuckets);
        }

        // Reads the input stream of the S3 object. Transforms content to JSON format.
        // Return the transformed text in a File object.
        private static string TransformText(string bucketName, string fileKey)
        {
            string transformedText = null;
            StringBuilder sbJSON = new StringBuilder();
            string line;

            try
            {
                GetObjectRequest request = new GetObjectRequest()
                {
                    BucketName = bucketName,
                    Key = fileKey
                };
                GetObjectResponse response = s3ForStudentBuckets.GetObject(request);

                //transformtojson then write to file **
                StreamReader reader = new StreamReader(response.ResponseStream);
                while((line = reader.ReadLine()) != null)
                {
                    //transform
                    sbJSON.Append(TransformLineToJson(line));
                    
                }
                reader.Close();
            }
            catch (IOException ex)
            {
                Debug.WriteLine("Transformer: Unable to create transformed file");
                Debug.WriteLine(ex.Message);
            }

            transformedText = sbJSON.ToString();
            return transformedText;
        }

        private static string TransformLineToJson(string inputLine)
        {
            string[] inputLineParts = inputLine.Split(',');
            int len = inputLineParts.Length;

            string jsonAttrText = "{\n  " + JsonComment + "\n";
            for (int i = 0; i < len; i++)
            {
                jsonAttrText = jsonAttrText + "  \"" + Attributes[i] + "\"" + ":" + "\"" + inputLineParts[i] + "\"";
                if (i != len - 1)
                {
                    jsonAttrText = jsonAttrText + ",\n";
                }
                else
                {
                    jsonAttrText = jsonAttrText + "\n";
                }
            }
            jsonAttrText = jsonAttrText + "},\n";
            return jsonAttrText;
        }
    }
}
