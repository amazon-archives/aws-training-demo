#Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

from unittest import TestCase

import unittest

import boto3
from botocore.exceptions import BotoCoreError,NoCredentialsError,ClientError
from s3_lab import DataManager


class TestDataManager(TestCase):

    def test_output_files_exist(self):
        d = DataManager()
        l1 = 0
        l2 = 0
        try:
            self.s3 = boto3.resource('s3')
        except BotoCoreError as e:
            if isinstance(e, NoCredentialsError):                           
                print("No AWS Credentials file found or credentials were invalid")                                                      
            else:                                                                                   
                print("Error message -" + str(e))   
            sys.exit()

        self.bucketDest = self.s3.Bucket(d.OUTPUT_BUCKET_NAME)
        exists = True
        try:
            self.s3.meta.client.head_bucket(Bucket=d.OUTPUT_BUCKET_NAME)
        except ClientError as e:
            # If a client error is thrown, then check that it was a 404 error.If it was a 404 error, then the bucket does not exist.
            error_code = int(e.response['Error']['Code'])
            print(e)
            self.assertTrue(self, 1 == 0)
            if error_code == 404:
                exists = False
        if exists:
            self.list=self.s3.meta.client.list_objects(Bucket=d.INPUT_BUCKET_NAME)['Contents']

            for s3_key in self.list:
                s3_object = s3_key['Key']

                if not s3_object.endswith("/"):
                    l1 += 1

            self.list2=self.s3.meta.client.list_objects(Bucket=d.OUTPUT_BUCKET_NAME)['Contents']
            for s3_key in self.list2:
                s3_object = s3_key['Key']

                if not s3_object.endswith("/"):
                    l2 += 1
        self.assertTrue(self, l1 == l2)

if __name__ == '__main__':
    unittest.main()
