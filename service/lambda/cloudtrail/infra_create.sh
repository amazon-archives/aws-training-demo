# 
# Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


# Default values
. ./infra_parameters.sh

#
# Create Cloud Trail & S3 bucket
#
status "Creating S3 bucket and activating CloudTrail"
aws cloudtrail create-subscription --region $REGION --profile $PROFILE --s3-new-bucket $BUCKETNAME --name $TRAILNAME

# sometime the above call fails with "A client error (InsufficientS3BucketPolicyException) occurred when calling the CreateTrail operation: Incorrect S3 bucket policy is detected for the bucket: cloudtrail-logs-sbp"
#
# when this happens, run the teardown script and try again

#
# Create SNS Topic and register your email address
#
status "Creating SNS Topic"
TOPIC_ARN=$(aws sns create-topic --name $TOPICNAME --profile $PROFILE  --region $REGION --output text)
aws sns subscribe --protocol email --topic-arn $TOPIC_ARN --notification-endpoint $EMAIL --region $REGION --profile $PROFILE
echo $TOPIC_ARN >> TOPIC_ARN.do-not-delete

#
# Create IAM Policies for Lambda execution
#
status "Creating IAM Execution Role"
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

ROLE_EXEC_ARN=$(aws iam create-role --role-name $ROLEEXECNAME --assume-role-policy-document file://./role_execution_trust_policy.json --query 'Role.Arn' --profile $PROFILE  --output text)

cat > role_execution_policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["logs:PutLogEvents"],
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

aws iam put-role-policy --role-name $ROLEEXECNAME --policy-name $POLICYEXECNAME --policy-document file://./role_execution_policy.json --profile $PROFILE


#
# Modify the Lambda function to include the name of the bucket to hold the configuration file
#
status "Uploading configuration file"
cp cloudtrail.js cloudtrail.js.orig
sed -i $SED_OPTS "/^var FILTER_CONFIG_BUCKET =/s/BUCKETNAME/$BUCKETNAME/g" cloudtrail.js

cp filter_config.json filter_config.json.orig
sed -i $SED_OPTS "s/REGION/$REGION/g" filter_config.json
sed -i $SED_OPTS "s/TOPICARN/$TOPIC_ARN/g" filter_config.json

# Upload Lambda function to configuration bucket
aws s3 cp filter_config.json s3://$BUCKETNAME/filter_config.json --region $REGION --profile $PROFILE

#
# Package the lambda function
#
status "Install Node dependencies"
npm install aws-sdk q
status "Packaging Lambda function"
rm cloudtrail.zip ; zip -r cloudtrail.zip cloudtrail.js node_modules -x node_modules/aws-sdk/\*

#
# Upload the function
#
status "Uploading Lambda function"
sleep 5 #wait for IAM role to be available to lambda - :-(

FUNCTION_ARN=$(aws lambda create-function --profile $PROFILE --region $REGION --zip-file fileb://./cloudtrail.zip --function-name $FUNCTIONNAME --runtime nodejs --role "$ROLE_EXEC_ARN" --handler cloudtrail.handler --query FunctionArn --output text)

#
# Add permission to authorize S3 bucket to invoke Lambda
#
AWS_ACCOUNT_ID=$(echo $ROLE_EXEC_ARN | sed 's/^arn:aws:iam::\(.*\):.*$/\1/')
aws lambda add-permission --function-name $FUNCTIONNAME --region $REGION --profile $PROFILE --statement-id Id-x  --action "lambda:InvokeFunction" --principal s3.amazonaws.com --source-arn arn:aws:s3:::$BUCKETNAME --source-account $AWS_ACCOUNT_ID

#
# Configure S3 to send notifications to Lambda
#
status "Configuring S3 Notifications"
sleep 5
cat > s3_notifications_config.json <<EOF
{
    "CloudFunctionConfiguration": {
        "CloudFunction": "$FUNCTION_ARN",
        "Id": "cloudtrail-lambda-notifications",
        "Event": "s3:ObjectCreated:*"
    }
}
EOF

aws s3api put-bucket-notification --notification-configuration file://./s3_notifications_config.json --bucket $BUCKETNAME --profile $PROFILE --region $REGION

echo "Account ID:   " $AWS_ACCOUNT_ID
echo "Role ARN:     " $ROLE_EXEC_ARN
echo "Topic ARN:    " $TOPIC_ARN
echo "Function ARN: " $FUNCTION_ARN
