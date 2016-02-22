# Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import boto3
import sys
import csv
import json
from botocore.exceptions import ClientError,NoCredentialsError,BotoCoreError
import s3_lab_setup as s3setup


INPUT_BUCKET_NAME = "student-bucket-in8"                   # STUDENT TODO: Update input bucket name to a unique bucket name.
OUTPUT_BUCKET_NAME = "student-bucket-out8"               # STUDENT TODO: Update output bucket name to a unique bucket name.


class DataManager:

    s3 = None
    bucketSource = None
    bucketDest = None


    def __init__(self):

        #Before running the code, check that the ~/.aws/credentials file has your credentials.

        self.INPUT_BUCKET_NAME = INPUT_BUCKET_NAME
        self.OUTPUT_BUCKET_NAME = OUTPUT_BUCKET_NAME

        #The Amazon S3 resource allows you to manage buckets and objects programmatically.
        #STUDENT TODO: Create an S3 connection by creating an Amazon S3 resource object.

        self.s3 = boto3.resource('s3')                                                                    #@Del

        try:
            #Failed to setup input bucket
            self.r=self.s3.meta
            #Create input bucket
            self.bucketSource = s3setup.setupInputBucketWithFiles(inputbucket=self.INPUT_BUCKET_NAME)
        except AttributeError as e:
            print('Failed to create input bucket')
            print(e)
            sys.exit()

        exists = True

        try:
            self.s3.meta.client.head_bucket(Bucket=self.INPUT_BUCKET_NAME)
        except NoCredentialsError as e:
            print("No AWS Credentials file found or credentials were invalid")
            sys.exit()
        except ClientError as e:
            # If a client error is thrown, then check that it was a 404 error.If it was a 404 error, then the bucket does not exist.
            print(e)
            error_code = int(e.response['Error']['Code'])
            if error_code == 404:
                exists = False
            else:
                print('Change the bucket name in case the bucket name is already taken.')

        if exists:
            self.retrieve()
            #STUDENT TODO: Add user metadata
            self.transform()                                                                               #@Del
        else:
            print('Access denied on Input bucket ...')


    def retrieve(self):
        self.objectList = None

        #Retrieve the file list from bucket
        try:                                                                                                #@Del
            self.objectList = self.s3.meta.client.list_objects(Bucket=self.INPUT_BUCKET_NAME)['Contents']         #@Del
        except ClientError as ex:                                                                           #@Del
            print(ex)                                                                                       #@Del
            print('Bucket Permissions Error')                                                                #@Del
        except NoCredentialsError as ex:                                                                    #@Del
            print('No AWS Credentials file found or credentials were invalid')                                                                    #@Del
            print(ex)                                                                                       #@Del

        if self.objectList == None:
            print('File download failed')

        for s3_key in self.objectList:
            s3_object = s3_key['Key']

            #Download each file using the obtained file list
            if not s3_object.endswith("/"):                                                                 #@Del
                self.s3.meta.client.download_file(self.INPUT_BUCKET_NAME, s3_object, s3_object)             #@Del
            else:                                                                                           #@Del
                if not os.path.exists(s3_object):                                                           #@Del
                    os.makedirs(s3_object)                                                                  #@Del


    def transform(self):
        #Reads the input stream of the S3 object. Transforms content to JSON format.
        #Return the transformed text in a File object.
        print('Transformer: Here we go...')

        for s3_key in self.objectList:
            s3_object = s3_key['Key']

            if not s3_object.endswith("/"):
                print('Transformer: Transforming file ' + s3_object)
                f = open( s3_object, 'rU' )

                #Get the Headings
                fn = f.readlines(1)[0].split(',')

                reader = csv.DictReader( f, fieldnames = fn )
                #Convert to JSON formaat
                out = json.dumps( [ row for row in reader ] )
                f.close()
                print(s3_object)

                #Store the JSON
                name = s3_object.split('.')[0]
                key = s3_object.split('.')[0]+".json"

                name =  key
                f = open( name, 'w+')
                f.write(out)
                f.close()
                self.upload(name,key)
        print('Transformer: DONE')

    def upload(self, name, key):

        self.bucketDest = self.s3.Bucket(self.INPUT_BUCKET_NAME)
        exists = True
        #Create the output bucket if it does not exist already.
        try:
            self.s3.meta.client.head_bucket(Bucket=self.OUTPUT_BUCKET_NAME)
        except ClientError as e:
            # If a client error is thrown, then check that it was a 404 error.If it was a 404 error, then the bucket does not exist.
            error_code = int(e.response['Error']['Code'])
            if error_code == 404:
                exists = False
        if not exists:
            print('Transformer: Creating output bucket ' + self.OUTPUT_BUCKET_NAME)
            self.s3.create_bucket(Bucket=self.OUTPUT_BUCKET_NAME, CreateBucketConfiguration={'LocationConstraint': s3setup.LAB_REGION})

        # Upload files to bucket
        # STUDENT TODO: Enable server side encryption.
        self.s3.meta.client.upload_file(name,self.OUTPUT_BUCKET_NAME,key,ExtraArgs={'ServerSideEncryption':'AES256','Metadata': {'contact': 'John Doe'}}) #@Del
        self.generatePresignedURL(self.OUTPUT_BUCKET_NAME,key)


    def generatePresignedURL(self,bucket,key):
        #List used to store pre-signed URLs generated.
        print('Transformer: Pre-signed URLs ')
        url = None
        #STUDENT TODO: Generate a pre-signed URL to retrieve object (GET)
        url = self.s3.meta.client.generate_presigned_url('get_object',Params={'Bucket':bucket,'Key':key})          #@Del
        print(url)



if __name__ == '__main__':
    DataManager()
    print("End of the lab")
