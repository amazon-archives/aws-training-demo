# process-commoncrawl-with-emr
A short demo that shows how to launch an EMR cluster with spot instances using the CLI, copy a part of the commonCrawl AWS public data set using s3distCP and how to use the grep implementation from the Hadoop examples jar to find what Big Data is


- AWS Public Data Sets (http://aws.amazon.com/public-data-sets/)
- The commonCrawl project (http://commoncrawl.org/)

To do all the following with a single command please see "Step 7: Automate"

##Step 1: Launch the cluster
The example was tested with the 3.0.4 EMR AMI (With newer versions an error might appear when running the s3DistCP)
When launched in Ireland with a m1.large master node and 4 c3.8xlarge core nodes,
The copy should take around 3 minutes and the grep around 2.5 minutes.
The cost per hour with the bid prices in the example should be up to : 1.43 $
```
 aws emr create-cluster --ami-version 3.0.4 --name "<Cluster name>" --region <region> --no-auto-terminate\
 --instance-groups InstanceGroupType=MASTER,InstanceCount=1,BidPrice=<Bid price for master node>,InstanceType="<instance size>"\
 InstanceGroupType=CORE,InstanceCount=<number of core nodes>,BidPrice=<Bid price for core nodes>,InstanceType="<instance size>"\
 --service-role EMR_DefaultRole --ec2-attributes\ InstanceProfile=EMR_EC2_DefaultRole,KeyName=<your-ec2-key-pair>,AvailabilityZone=eu-west-1a\
 --tags project=big-data application=common-crawl --applications Name=Ganglia
 ```
 Example:
 ```
 aws emr create-cluster --ami-version 3.0.4 --name "My cluster" --region eu-west-1 --no-auto-terminate\
 --instance-groups InstanceGroupType=MASTER,InstanceCount=1,BidPrice=0.03,InstanceType="m1.large"\
 InstanceGroupType=CORE,InstanceCount=4,BidPrice=0.35,InstanceType="c3.8xlarge"\
 --service-role EMR_DefaultRole --ec2-attributes InstanceProfile=EMR_EC2_DefaultRole,KeyName=myKey,AvailabilityZone=eu-west-1a\
 --tags project=big-data application=common-crawl cost-center=1234 --applications Name=Ganglia
 ```

##Step 2: Copy part of the commonCrawl data set from s3 to HDFS
The folder size under the specified path is 55.6GB (feel free to include more paths).
At the time of writing s3distCP doesn't support multiple S3 source paths,
one could use distCP instead or use srcPattern with s3distCP.
 ```
 hdfs dfs -mkdir /grep-data/
 ```
 ```
 hadoop jar /home/hadoop/lib/emr-s3distcp-1.0.jar --s3Endpoint s3-external-1.amazonaws.com --src\
 "s3://aws-publicdatasets/common-crawl/crawl-data/CC-MAIN-2014-23/segments/1406510280868.21/wet/" --dest "hdfs:///grep-data/"
 ```
 Example for using source pattern to include multiple paths (A larger data set):
 ```
 hadoop jar /home/hadoop/lib/emr-s3distcp-1.0.jar --s3Endpoint s3-external-1.amazonaws.com --src\
 "s3://aws-publicdatasets/common-crawl/crawl-data/CC-MAIN-2014-23/segments/" --dest "hdfs:///grep-data1/" --srcPattern '.*(1404776400583.60|1404776400808.24)\/wet\/.*'
 ```

##Step 3: Check the size of the data set copied
```
hdfs dfs -du -s -h /grep-data
```
##Step 4: Use the grep utility from the hadoop examples file
The data files provided in the commonCrawl data set are compressed.
the following job will grep through 130GB of uncompressed data.
```
hadoop jar hadoop-examples.jar grep /grep-data/ /grep-output/ '[Bb]ig [Dd]ata is ([a-zA-Z]{5,})'
```
##Step 5: View results
```
hdfs dfs -cat /grep-output/part-r-00000 | head -10
```
##Step 6: View cluster performance with Ganglia
Edit the "ElasticMapReduce-master" secruity group to allow access to port 80 from your IP
or create an SSH tunnel as described under "View cluster details"/Connections.
Navigate to <master-node-dns-name>/ganglia and explore the CPU/Mem/Network metrics for all the instances.

##Step 7: Automate

- Make sure you have an S3 bucket, a preferably newly created folder and that you edit the steps.json to include the correct output path. example : "s3://my-common-crawl/output"
*If the specified folder contains the results of a previously invoked job the new job will fail because the output already exists. Make sure the output folder is empty for each run.

- If you're interested in things other than Big Data, feel free to change the following line:
"[Bb]ig [Dd]ata is ([a-zA-Z]{5,})" to something like "[Ll]ife is ([a-zA-Z]{5,})" or any valid regular expression.

- Create a steps.json file :
```
  [
    {
      "Name": "Copy part of the common crawl data set",
      "Jar" : "file:///home/hadoop/lib/emr-s3distcp-1.0.jar",
      "Args": [
        "--s3Endpoint","s3-external-1.amazonaws.com","--src","s3://aws-publicdatasets/common-crawl/crawl-data/CC-MAIN-2014-23/segments/1406510280868.21/wet/","--dest","hdfs:///grep-data/"
        ]
      ,
      "ActionOnFailure": "CONTINUE",
      "Type": "CUSTOM_JAR"
    },
    {
      "Name": "Grep using the hadoop-examples JAR",
      "Jar" : "file:///home/hadoop/hadoop-examples.jar",
      "Args": [
        "grep","/grep-data/","s3://<REPLACE WITH S3 PATH>","[Bb]ig [Dd]ata is ([a-zA-Z]{5,})"
        ],
      "ActionOnFailure": "TERMINATE_JOB_FLOW",
      "Type": "CUSTOM_JAR"
    }
  ]
```
- Launch the cluster using the steps.json to describe the steps.
*Don't forget to edit the name of the key pair and to provide the path to the steps.json
(Unless you're running the command from the same directory)
Example:
```
aws emr create-cluster --ami-version 3.0.4 --name "My cluster" --region eu-west-1 --auto-terminate\
 --instance-groups InstanceGroupType=MASTER,InstanceCount=1,BidPrice=0.03,InstanceType="m1.large"\
 InstanceGroupType=CORE,InstanceCount=4,BidPrice=0.35,InstanceType="c3.8xlarge"\
 --service-role EMR_DefaultRole --ec2-attributes InstanceProfile=EMR_EC2_DefaultRole,\
 KeyName=<YOUR KEY PAIR>,AvailabilityZone=eu-west-1a\
 --steps file://./step-prod.json --tags project=big-data application=common-crawl cost-center=1234 --applications Name=Ganglia
```
- The "--auto-terminate" arg will make sure the cluster terminates after the steps complete.
- Get the output from the part-r-00000 file that should appear in the S3 path specified (You might have to make the object public in case you're accessing it by using the S3 console)

The End.

Cloned from https://github.com/stasov/process-commoncrawl-with-emr 
