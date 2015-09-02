# Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

#!/bin/bash
  #Get instanceID
  IP=$(curl http://169.254.169.254/latest/meta-data/instance-id -s)
  #Check if sysstat was install
  /usr/bin/which sar
   if [ "$?" != "0" ];then
     echo "Sar Could be found?"
     exit 3
   fi
  #The below sysstat is just a POC of gathering and "Agregating metrics" which can be sent to cloudwatch already Aggregated
  #Of course in real prod scenarion you would proably pull multiple counters here and then perfrom your own math...
  cswch=`sar 1 5 -w | grep Average | awk '{ print $3 }'`
  /usr/bin/aws cloudwatch put-metric-data --metric-name ContextSwitchesPerSecAvg --namespace "System/Linux" --statistic-value Sum=${cswch},Minimum=${cswch},Maximum=${cswch},SampleCount=5 --dimensions "InstanceId=${IP}" --region eu-west-1


    if [ "$?" != "0" ];then

     echo "Not Cool, AWS Command failed, Path?IAM Role? do not be lazy just run it interactivly and capture the error"
     #DEbug here
     echo $IP
     echo $cswch
     exit 3

    fi
exit 0
