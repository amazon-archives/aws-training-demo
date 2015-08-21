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

# Unsubscribe Cloud Trail & Delete S3 bucket
status "Disabling CloudTrail and Deleting Bucket"
aws s3 rm --recursive s3://$BUCKETNAME/ --region $REGION --profile $PROFILE
aws s3 rb s3://$BUCKETNAME/ --region $REGION --profile $PROFILE
aws cloudtrail delete-trail --name $TRAILNAME --region $REGION --profile $PROFILE

# Delete SNS Topic and Subscription
status "Deleting SNS Topic"
aws sns delete-topic --topic-arn `cat TOPIC_ARN.do-not-delete` --profile dalek --region $REGION --profile $PROFILE
rm TOPIC_ARN.do-not-delete

# Delete IAM role
status "Deleting IAM Role"
aws iam delete-role-policy --role-name $ROLEEXECNAME --policy-name  $POLICYEXECNAME --profile $PROFILE
aws iam delete-role --role-name $ROLEEXECNAME --profile $PROFILE
rm role_execution_policy.json role_execution_trust_policy.json

# Delete the Lambda function
status "Deleting Lambda Function"
aws lambda delete-function --function-name $FUNCTIONNAME --region $REGION --profile $PROFILE

status "Cleaning up"
rm cloudtrail.zip
rm -rf node_modules
rm s3_notifications_config.json
mv cloudtrail.js.orig cloudtrail.js
mv filter_config.json.orig filter_config.json

status "Done"
