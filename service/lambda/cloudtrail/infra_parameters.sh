# 
# Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

##########################################
#
# YOU ARE REQUIRED TO CHANGE THESE VALUES
#
##########################################

# CHANGEME : Choose a globally unique bucket name
BUCKETNAME=cloudtrail-logs-sst8

# CHANGEME : Use your email address
EMAIL=stormacq@amazon.lu

# (optional) CHANGEME : adjust region name
REGION=us-west-2

# (optional) CHANGEME : Change to your profile name
PROFILE=default

##########################################
#
# No changes required below this line
#
##########################################

BASENAME=lambda-cloudtrail
TRAILNAME=$BASENAME
TOPICNAME=$BASENAME
ROLEEXECNAME=$BASENAME-exec
ROLEINVOKENAME=$BASENAME-invoke
POLICYEXECNAME=lambda-cloudtrail-analysis
POLICYINVOKENAME=s3-invoke-lambda
FUNCTIONNAME=$BASENAME
if [ `uname -s` == 'Darwin' ]; then SED_OPTS="''"; else SED_OPTS=""; fi

function status {
   color='\033[0;34m' # blue
   nc='\033[0m'       # no color
   echo -e "${color}$1${nc}"
}
