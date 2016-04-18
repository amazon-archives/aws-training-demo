# Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

# TODO 
#
# 1. Create a Role for the lambda function itself
# 2. Replace EXEC_ROLE with your Role's ARN
# 3. Adjust CLI profile to match your config (default will work in most cases)
# 4. Adjust Region

REGION=eu-west-1
EXEC_ROLE=arn:aws:iam::5xxxxxxxxxx8:role/Lamba-Mobile_Lambda
ZIP_FILE=mobile-lambda.zip
CLI_PROFILE=admin
PAYLOAD="{\"firstName\": \"Seb\",\"lastName\": \"Sto\"}"
CLIENT_CONTEXT="{\"env\": {\"make\" : \"testing device\"}}"

echo "Packaging Code"
zip $ZIP_FILE mobile-lambda.js

echo "Deleting old function"
aws lambda delete-function --function-name mobile-lambda --region $REGION --profile $CLI_PROFILE

echo "Uploading new function"
aws lambda create-function --region $REGION --function-name mobile-lambda --runtime nodejs --role $EXEC_ROLE --handler mobile-lambda.handler --zip-file fileb://./$ZIP_FILE --profile $CLI_PROFILE

echo "Invoke for testing"
aws lambda invoke --function-name mobile-lambda --region $REGION --profile $CLI_PROFILE --payload "$PAYLOAD" --client-context `echo $CLIENT_CONTEXT | base64` result.json
cat result.json
rm result.json
