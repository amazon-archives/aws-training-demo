# Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


#!/bin/bash -x

exec 3>&1 # "save" stdout to fd 3
exec &>> /home/ec2-user/create.log

function error_exit() {
    echo "{\"Reason\": \"$1\"}" >&3 3>&- # echo reason to stdout (instead of log) and then close fd 3
    exit $2
}

if [ -z "${Event_ResourceProperties_Version}" ]
then
    error_exit "Version is required." 64
fi


AMI=$(/home/ec2-user/findAMI.py -a ${Event_ResourceProperties_Version})
AMI_ret=$?
if [ $AMI_ret -ne 0 ]
then
    error_exit "findAMI.py failed." $AMI_ret
else
    echo "{ \"PhysicalResourceId\" : \"$AMI\" }" >&3 3>&-  # echo success to stdout (instead of log) and then close fd 3
    exit 0
fi
