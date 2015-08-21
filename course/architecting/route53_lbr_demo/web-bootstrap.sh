# Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


#!/bin/bash -x

# send std out to log file
exec &>> /home/ec2-user/bootstrap.log

function error_exit() {
    echo "{\"Reason\": \"$1\"}"
    exit $2
}

echo "Installing Apache's HTTPD and PHP"
RETURN_CODE = $(/usr/bin/yum -y install httpd php)
if [ $RETURN_CODE -ne 0 ]
then
    error_exit "yum install httpd php failed" $RETURN_CODE
fi

RETURN_CODE=$(/sbin/chkconfig httpd on)
if [ $RETURN_CODE -ne 0 ]
then
    error_exit "chkconfig failed" $RETURN_CODE
fi

RETURN_CODE=$(/etc/init.d/httpd start)
if [ $RETURN_CODE -ne 0 ]
then
    error_exit "httpd start failed" $RETURN_CODE
fi

cd /var/www/html
RETURN_CODE=$(wget http://aws-emea.info.s3.amazonaws.com/resources/route53-lbr/web-examplefiles.zip)
if [ $RETURN_CODE -ne 0 ]
then
    error_exit "download sample web app failed" $RETURN_CODE
fi

RETURN_CODE=$(unzip web-examplefiles.zip)
if [ $RETURN_CODE -ne 0 ]
then
    error_exit "unzip web app failed" $RETURN_CODE
fi
