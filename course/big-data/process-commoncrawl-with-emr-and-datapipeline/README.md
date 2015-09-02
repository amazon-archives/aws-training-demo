# process-commoncrawl-with-emr-and-datapipeline

A demo that shows how to orchestrate data transformation with the Data Pipeline service.
For a partial manual step by step guide see :
https://github.com/stasov/process-commoncrawl-with-emr

A predefined Data Pipeline template is used to perform the following steps:
- Launch an EMR cluster
- Copy a part of the CommonCrawl public data set locally to the cluster (HDFS)
- Process the data (Run a regex search with the following pattern : [Bb]ig [Dd]ata is ([a-zA-Z]+) by using the grep utility from hadoop-examples.jar)
- Copy the processed data to a specified dynamoDB table

To learn more about AWS public data sets or the CommonCrawl project, see:
- AWS Public Data Sets (http://aws.amazon.com/public-data-sets/)
- The commonCrawl project (http://commoncrawl.org/)

Choose a region where Data Pipeline is supported and use it where \<your-region\> is specified.
The demo was tested in Ireland (eu-west-1) and Oregon (us-west-2) but should work just
as well in any other region.

##Step 1: Create the needed resources
Create a S3 bucket:
```
aws s3 mb s3://<your-bucket> --region <your-region>
 ```
Create a DynamoDB table as follows (The Primary Hash key setup is important):
```
aws dynamodb create-table --region <your-region> --table-name <your-dynamo-table-name> --attribute-definitions AttributeName=phrase,AttributeType=S --key-schema AttributeName=phrase,KeyType=HASH --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1
```

##Step 2: Edit the artifacts and upload to your S3 bucket
Download and edit the following files:
emr.py - replace \<your-region\> and \<your-dynamo-table-name\> with the appropriate values
data-pipe-line.template - no need to edit
Upload the files to your S3 bucket.

##Step 3: Create and activate the Data pipeline
In the Data Pipeline console:
- choose a name for the pipeline  
- choose "import a definition", then "Load from S3" and browse to the data-pipe-line.template object in your bucket.  
In case you are prompt with an "Access denied" error, either set appropriate permissions on the data-pipe-line.template file (for example make it public) or download the file locally and use the "load local file" option.  
- Supply a region name, bucket name and key-pair name for the instances of the cluster
- Leave "Path to a part of the common crawl data set" unchanged (It's useful if you'll decide to process a different part of the CommonCrawl project in the future)
- Leave everything else with the default settings and activate the pipeline
If you are prompt with a warning, ignore it and activate the pipeline.

##Step 4: Monitor the progress and view results in your dynamoDB table
- Clone the activated pipeline and view the different configurations in the "Architect Editor"  
- View the properties of the cluster in the EMR console  
- Take a look at the steps that are submitted to the cluster by Data Pipeline  

The process should take around 30 minutes.  
It's time for a coffee break.  
Take a look at the results in the dynamoDB table.  
What is the most common phrase for "Big Data is" ?

Remember to clean up!
Delete the dynamoDB table, the S3 bucket and the pipeline in Data pipeline.

The End.  



Cloned from https://github.com/stasov/process-commoncrawl-with-emr-and-datapipeline
