# Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.



import boto3
import sys
import csv
import os
from botocore.exceptions import ClientError,NoCredentialsError,BotoCoreError


INPUT_BUCKET_NAME = "student-bucket-18"


BUCKET_WITH_FILES = "us-west-2-aws-training"


LAB_BUCKET_DATA_FILE_KEYS=['awsu-ilt/developing/v2.0/lab-2-s3/static/SampleInputFiles/DrugAdverseEvents_September.txt',
                            'awsu-ilt/developing/v2.0/lab-2-s3/static/SampleInputFiles/DrugAdverseEvents_October.txt']



STUDENT_BUCKET_DATA_FILE_KEYS=['DrugAdverseEvents_September.txt',
                               'DrugAdverseEvents_October.txt']

# STUDENT TODO: Set the region in which the lab is running.
LAB_REGION="us-west-2"


def setupInputBucketWithFiles(inputbucket=INPUT_BUCKET_NAME):

    downloadFiles()
    setup(inputbucket)

def downloadFiles():

    s3 = boto3.resource('s3', 'us-west-2')
    try:
        s3.meta.client.head_bucket(Bucket=BUCKET_WITH_FILES)
    except NoCredentialsError as e:
        print("No AWS Credentials file found or credentials were invalid")
        sys.exit()

    for index1 in range(len(STUDENT_BUCKET_DATA_FILE_KEYS)):
        name=LAB_BUCKET_DATA_FILE_KEYS[index1]
        key=STUDENT_BUCKET_DATA_FILE_KEYS[index1]
        #urllib.request.urlretrieve(name,key)
        s3.meta.client.download_file(BUCKET_WITH_FILES,  name, key)

        print('File downloaded '+key)

def setup(inputbucket):
    s3 = boto3.resource('s3')
    bucketDest = s3.Bucket(inputbucket)
    exists = True
    try:
        s3.meta.client.head_bucket(Bucket=inputbucket)
    except NoCredentialsError as e:
        print("No AWS Credentials file found or credentials were invalid")
        sys.exit()
    except ClientError as e:
        # If a client error is thrown, then check that it was a 404 error.If it was a 404 error, then the bucket does not exist.
        error_code = int(e.response['Error']['Code'])
        if error_code == 404:
            exists = False
    if not exists:
        print('Transformer: Creating output bucket: ' + inputbucket)
        s3.create_bucket(Bucket=inputbucket, CreateBucketConfiguration={'LocationConstraint': LAB_REGION})

    print('Bucket created' + inputbucket)

    for index1 in range(len(STUDENT_BUCKET_DATA_FILE_KEYS)):
        name=LAB_BUCKET_DATA_FILE_KEYS[index1]
        key=STUDENT_BUCKET_DATA_FILE_KEYS[index1]
        try:
            s3.meta.client.upload_file(key,inputbucket,key)
        except NoCredentialsError as e:
            print("No AWS Credentials file found or credentials were invalid")
            sys.exit()
        except ClientError as e:
        # If a client error is thrown, then check that it was a 404 error.If it was a 404 error, then the bucket does not exist.
            print(e)
            print('Access denied during uploading. Upload failed.')

        print('Uploaded file' + key)

if __name__ == '__main__':
    setupInputBucketWithFiles()
