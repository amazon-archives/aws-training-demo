# CloudTrail log analyser for Lambda

This sample code demonstrates the use of Lambda to analyse CloudTrail log files and to post SNS notifications when unexpected API usage is detected.

This code supports the blog post on the [AWS Security Blog] (http://blogs.aws.amazon.com/security/post/Tx2ZTUVN2VGBS85/How-to-Receive-Alerts-When-Specific-APIs-Are-Called-by-Using-AWS-CloudTrail-Amaz).

## Automatic Setup

We provide you two scripts to automatically create and teardown the infrastructure required to run the Lambda function (SNS, S3, CloudTrail) and to deploy the function itself.

Execute ```infra_create.sh``` to create the infrastructure required to run the function and to deploy the function.

When you are done, execute ```infra_teardown.sh``` to terminate and delete all resources, so that you will not incur any cost related to this environment.

When using these scripts, be sure to adapt a couple of values to your specific environment.
Each variable that you must modify are at the top of ```infra_parameters.sh``` script.

Parameters are :

- ```BUCKETNAME``` : where CloudTrail will deliver its log files.  This name must be globally unique
- ```EMAIL``` : the email address where you want receive SNS Push Notifications
- ```REGION``` : the region where you want to deploy the infrastructure
- (optional) ```PROFILE``` : the AWS CLI profile to use (default value is ```default```)


## Manual Setup

### Position some environment variable

```
EMAIL=youremail@company.com
REGION=us-west-2
BUCKETNAME=lambda-cloudtrail
TRAILNAME=lambda-cloudtrail
TOPICNAME=lambda-cloudtrail
FUNCTIONNAME=lambda-cloudtrail
```

### Create SNS Topic

```
TOPIC_ARN=$(aws sns create-topic --name $TOPICNAME  --region $REGION --output text)
aws sns subscribe --protocol email --topic-arn $TOPIC_ARN --notification-endpoint $EMAIL --region $REGION
```

Take note of the ARN of your role, you will need it at step 3 below.

### Create IAM Role for Lambda execution

```
cat > role_execution_trust_policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "",
      "Effect": "Allow",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF

ROLE_EXEC_ARN=$(aws iam create-role --role-name $ROLEEXECNAME --assume-role-policy-document file://./role_execution_trust_policy.json --query 'Role.Arn' --output text)

cat > role_execution_policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["logs:*"],
      "Resource": "arn:aws:logs:*:*:*"
    },
    {
      "Effect": "Allow",
      "Action": ["s3:GetObject"],
      "Resource": ["arn:aws:s3:::$BUCKETNAME/*"]
    },
    {
      "Effect": "Allow",
      "Action": ["sns:Publish"],
      "Resource": ["$TOPIC_ARN"]
    }
  ]
}
EOF

aws iam put-role-policy --role-name $ROLEEXECNAME --policy-name $POLICYEXECNAME --policy-document file://./role_execution_policy.json
```

### Configure and Activate CloudTrail

```
aws cloudtrail create-subscription --region $REGION --s3-new-bucket $BUCKETNAME --name $TRAILNAME
```

### Adapt your configuration file

Adapt the content of ```filter_config.json``` to your

- region

- SNS Topic ARN

- (optional) Device Endpoint ARN

- (optional) the name of the API you want to be notified about


### Upload your configuration file

```aws s3 cp filter_config.json  s3://YOUR_BUCKET_NAME/filter_config.json --region $REGION```

Be sure to note your bucket name and key name as you will need these later.

## How to test locally ?

### Install dependencies

```npm install aws-sdk q```

### Provide an input file

See ```input.json```
Be sure to adapt line 23 and 30 to a real CloudTrail log file, available on your bucket.

### Change two lines of code

You need to adapt the code to your bucket and configuration file name

Please change line 18 and 19 in ```cloudtrail.js```

```
var FILTER_CONFIG_BUCKET = 'YOUR_BUCKET_NAME';
var FILTER_CONFIG_FILE   = 'filter_config.json';
```
When deploying using the automatic procedure, ```infra_create.sh``` script does that for you.

### Run and Debug !

```node main.js```

and observe log ouput. Fix any error that might happen before to deploy to Lambda.

## How to deploy to Lambda?

### Upload the Lambda function

1. Create a zip file
```rm cloudtrail.zip ; zip -r  cloudtrail.zip cloudtrail.js node_modules -x node_modules/aws-sdk/\*```
(or use ```zip.sh``` provided for your convenience)

2. Upload to Lambda
```aws lambda upload-function --function-zip  ./cloudtrail.zip --function-name cloudtrail --runtime nodejs --role "YOUR_LAMBDA_ROLE_ARN" --handler "cloudtrail.js" --mode event --region $REGION```

### Hook S3 and Lambda together

First, add permission to let S3 call Lambda:

```
aws lambda add-permission --function-name $FUNCTIONNAME --region $REGION --statement-id Id-x  --action "lambda:InvokeFunction" --principal s3.amazonaws.com --source-arn arn:aws:s3:::$BUCKETNAME --source-account $AWS_ACCOUNT_ID
```

Finally, configure S3 to send event notifications to Lambda:

```
cat > s3_notifications_config.json <<EOF
{
    "CloudFunctionConfiguration": {
        "CloudFunction": "$FUNCTION_ARN",
        "Id": "cloudtrail-lambda-notifications",
        "Event": "s3:ObjectCreated:*"
    }
}
EOF

aws s3api put-bucket-notification --notification-configuration file://./s3_notifications_config.json --bucket $BUCKETNAME --region $REGION
```

## License

Copyright 2015, Amazon Web Services

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
