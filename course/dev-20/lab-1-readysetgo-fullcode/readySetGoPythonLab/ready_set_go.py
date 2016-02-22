#Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.


import boto3
from botocore.exceptions import NoCredentialsError,ClientError
import sys


#Before running the code, update ~/.aws/credentials file with your credentials.

def getAllBuckets():
    try:
        s3 = boto3.resource('s3')
        buckets = []   
    except NoCredentialsError:
        print("No AWS Credentials file found or credentials were invalid")
        sys.exit()

    try:
        no_of_buckets = len(list(s3.buckets.all()))
        print("Number of buckets : " + str(no_of_buckets))
        return no_of_buckets

    except ClientError as ex:
        print(ex)
        return 0


if __name__ == '__main__':
    getAllBuckets()
