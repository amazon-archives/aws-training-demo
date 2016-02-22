// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

using Amazon.S3;
using Amazon.S3.Model;
using System.Diagnostics;

namespace Lab1
{
    // The ReadySetGo class lists the number of buckets in your account.
    public static class ReadySetGo
    {
        public static void Main()
        {
            Debug.WriteLine("============================================");
            Debug.WriteLine("Welcome to the AWS .NET SDK! Ready, Set, Go!");
            Debug.WriteLine("============================================");

            //The Amazon S3 client allows you to manage buckets and objects programmatically.
            IAmazonS3 s3Client = new AmazonS3Client();

            try
            {
                ListBucketsResponse response = s3Client.ListBuckets();
                int numBuckets = 0;
                if (response.Buckets != null && response.Buckets.Count > 0)
                {
                    numBuckets = response.Buckets.Count;
                }
                Debug.WriteLine("You have " + numBuckets + " Amazon S3 bucket(s)");
            }
            catch (Amazon.S3.AmazonS3Exception S3Exception)
            {
                //AmazonServiceException represents an error response from an AWS service.
                //AWS service received the request but either found it invalid or encountered an error trying to execute it.
                if (S3Exception.ErrorCode != null && (S3Exception.ErrorCode.Equals("InvalidAccessKeyId") || S3Exception.ErrorCode.Equals("InvalidSecurity")))
                {
                    Debug.WriteLine("Please check the provided AWS Credentials.");
                    Debug.Write("If you haven't signed up for Amazon S3, please visit http://aws.amazon.com/s3");
                    Debug.WriteLine(S3Exception.Message, S3Exception.InnerException);
                }
                else
                {
                    Debug.WriteLine("Error Message:    " + S3Exception.Message);
                    Debug.WriteLine("HTTP Status Code: " + S3Exception.StatusCode);
                    Debug.WriteLine("AWS Error Code:   " + S3Exception.ErrorCode);
                    Debug.WriteLine("Request ID:       " + S3Exception.RequestId);
                }
            }
            finally
            {
                s3Client.Dispose();
            };
        
        }
    }
}


