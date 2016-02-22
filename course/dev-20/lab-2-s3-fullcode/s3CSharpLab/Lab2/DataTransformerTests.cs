// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Amazon.S3.Model;
using Amazon.Runtime;
using System.IO;
using System.Diagnostics;

namespace Lab2
{
    [TestClass]
    public class DataTransformerTests
    {
        // @Test
        [TestMethod]
        public void TestAllFilesExistAndModifiedInOutput()
        {
            try
            {
                Init();

                ListObjectsRequest inputFileObjects;
                String fileKey = null;

                inputFileObjects = new ListObjectsRequest
                {
                    BucketName = DataTransformer.InputBucketName
                };

                ListObjectsResponse listResponse;
                do
                {
                    listResponse = DataTransformer.s3ForStudentBuckets.ListObjects(inputFileObjects);
                    foreach (S3Object obj in listResponse.S3Objects)
                    {
                        fileKey = obj.Key;
                        GetObjectRequest request = new GetObjectRequest()
                        {
                            BucketName = DataTransformer.OutputBucketName,
                            Key = fileKey
                        };
                        GetObjectResponse response = DataTransformer.s3ForStudentBuckets.GetObject(request);

                        StreamReader reader = new StreamReader(response.ResponseStream);
                        string content = reader.ReadToEnd();
                        
                        if(!content.Contains(DataTransformer.JsonComment))
                        {
                            Assert.Fail("Failure - Input file not transformed; output file does not contain JSON comment. " + fileKey);
                        }
                       
                      }

                    // Set the marker property
                    inputFileObjects.Marker = listResponse.NextMarker;
                } while (listResponse.IsTruncated);

            }
            catch (AmazonServiceException ase)
            {
                Console.WriteLine("Error Message:    " + ase.Message);
                Console.WriteLine("HTTP Status Code: " + ase.StatusCode);
                Console.WriteLine("AWS Error Code:   " + ase.ErrorCode);
                Console.WriteLine("Error Type:       " + ase.ErrorType);
                Console.WriteLine("Request ID:       " + ase.RequestId);
            }
            catch (AmazonClientException ace)
            {
                Console.WriteLine("Error Message: " + ace.Message);
            }
        }

        [TestInitialize]
        public void Init()
        {
            try
            {
                DataTransformer.Main();
            }
           catch (Exception e)
            {
                Debug.WriteLine("Error found: " + e.Message);
            }
        }
    }
}
