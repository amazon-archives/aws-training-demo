# Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

#!/bin/bash
### Script tp demostrate how to perform actions based on specific Tags!
##ver 0.1 Kobi Biton Initial
###
byTag="/tmp/describeAPIByTag.out"
report="/tmp/instances.report.log"
function cleanup()
{

  rm -f $byTag
  rm -f $report
}
function error_exit()
{

    echo "Fatal Error: $1 Will exit now"
    exit 1
}
function error_cont()
{
   echo "Error: $1"
}

function ok_cont()
{
    echo "$1"

}
function send_to_sns_topic()
{
  if [ ! -z $SNSARN ];then
    body=`cat $report`
    aws sns publish --topic-arn $SNSARN --message "$body" || error_cont "Fail to publish to SNS Topic $SNSARN, Permissions? Topic Exists?"
    ok_cont "Published message to Topic"
  fi
}


usage()
{
cat << EOF

usage: $0 -t ResourceType (instance / volume)  -a APIToExecute (Stop/Terminate) -k (TagKeyName) -v (TagValue) -s [SNSTopicARN-NAME]

This script will Filter Tags based on resource type and then will loop all tag key:value pairs , based on a condition will perform API Operations (Currently Supports Only Terminate and Run Instances API)

OPTIONS:

   -t      Mandatory: Resource Type, Currently only "instance" is supported!
   -a      Mandatory: API To execute aginst the instance Stop/Terminate
   -k      Mandatory: The Tag Key Name
   -v      Mandatory: The Tag Value
   -s      OPTIONAL:  SNSARNTopicName


describe-tags with Filter --> Sort --> Loop based on specific Tag --> Perform Action Based On Tag Name  --> Send message to SNS Topic

EOF
}

while getopts “t:a:k:v:s:?” OPTION
do
     case $OPTION in
         t)
             RESOURCETYPE=$OPTARG
             ;;
         a)
             API=$OPTARG
             ;;
         k)
             KEY=$OPTARG
             ;;
         v)
             VALUE=$OPTARG
             ;;
         s)
             SNSARN=$OPTARG
             ;;
         ?)
             usage
             exit 1
             ;;
     esac
done

if [[ -z $RESOURCETYPE ]] || [[ -z $API ]] || [[ -z $KEY ]] || [[ -z $VALUE ]]
then
     usage
     exit 1
fi

# Need to check if aws cli tools are installed!
if [ ! -f /usr/local/bin/aws ];then
  error_exit "Could not locate AWS CLI tools"
fi
###
###Lets get the list of resources based on type
case $RESOURCETYPE in

  instance)

          #aws ec2 describe-tags --filters 'Name=resource-type,Values=instance' --query 'Tags[*].[ResourceId, Key, Value]' --output text | sort | while read id myKey myValue ;do echo $id $myKey $myValue;done
          aws ec2 describe-tags --filters 'Name=resource-type,Values=instance' --query 'Tags[*].[ResourceId, Key, Value]' --output text | sort > $byTag
            for i in "${PIPESTATUS[@]}"
              do
                if [ $i != 0 ];then
                  error_exit "Could not describe tags check error output"
                fi
            done

             while read id myKey myValue

               do
                 echo $id $myKey $myValue
                   if [[ $myKey == $KEY ]] && [[ $myValue == $VALUE ]];then
 		     ok_cont "Instance ID: $id is True for Tag Pair $myKey:$myValue, Going to $API Instance ID: $id"
                       if [[ $API == "Stop" ]] || [[ $API == "stop" ]];then
      			 #but need to check we do not stop a stopped one :-)
 			 status=$(aws ec2 describe-instance-status --instance-ids $id --query 'InstanceStatuses[*].InstanceState.Name' --output text --include-all-instances)
                         if [ "$status" != "stopped" ];then
                           ok_cont "Stopping Instance : $id"
                           aws ec2 stop-instances --instance-ids $id || error_cont "Could not stop instance ID: $id"
			   echo "`date`: Instance ID:$id Key: $myKey Value: $myValue Was Stopped" >> $report
			   send_to_sns_topic
                         else
                           ok_cont "Instance ID: $id is already stopped doing nada.."
                         fi
                       elif [[ $API == "Terminate" ]] || [[ $API == "terminate" ]];then
			 #Terminate Instance Code here...
                         #but First we need to check we do not terminate a terminated one :-)
                         status=$(aws ec2 describe-instance-status --instance-ids $id --query 'InstanceStatuses[*].InstanceState.Name' --output text --include-all-instances)
                         if [ "$status" != "terminated" ];then
                           ok_cont "Terminating Instance : $id"
                           aws ec2 terminate-instances --instance-ids $id || error_cont "Could not terminate instance ID: $id"
			   echo "`date`: Instance ID:$id Key: $myKey Value: $myValue Was Terminated!" >> $report
			   send_to_sns_topic
                         else
                           ok_cont "Instance ID: $id is already terminated doing nada.."
                         fi
 		       else
                         error_exit "$API was unknown exiting"
                       fi

                   fi
               done < $byTag

 ;;

  volume)
          exit 1
 ;;

 *) error_exit "I do not know this Resource Type or I simply do not support it yet , bye!"

 ;;

esac
cleanup
exit 0
