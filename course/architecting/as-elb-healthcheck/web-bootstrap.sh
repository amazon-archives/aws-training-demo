# Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


#!/bin/bash
yum -y install httpd php
chkconfig httpd on
/etc/init.d/httpd start
cd /var/www/html
wget http://aws-emea.info.s3.amazonaws.com/resources/route53-lbr/web-examplefiles.zip
unzip web-examplefiles.zip
