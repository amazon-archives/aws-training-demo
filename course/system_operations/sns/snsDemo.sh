# Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

#!/bin/bash
### Script tp demostrate sns push message!
##ver 0.1 Kobi Biton Initial
###
# trap ctrl-c and call ctrl_c()
trap ctrl_c INT

function ctrl_c() {
  echo "** Trapped CTRL-C ** Or end of the script , Lets go and clean stuff..."
  aws sns delete-topic --topic-arn $arnTopic || error_exit "Could DELETE SNS Topic $arnTopic"
  ok_cont "Program has ended and cleaned up after it self bye bye!"
  exit 0
}


function error_exit()
{

    echo "Fatal Error: $1 Will exit now"
    ctrl_c
    exit 1
}

function ok_cont()
{
    echo "$1"

}

usage()
{
cat << EOF

usage: $0 -t TopicName -e ValidEmailAddress -m "Message To Publish"

This script will create an SNS demo! , it will require an AWS IAM user with creds/temp creds that is able to create/delete topics and send messages to topics as well!
You can use an IAM user , or an EC2 trusted role for this purpose

OPTIONS:
   -t      SNS Topic Name
   -e      Subscriber email address
   -m      Message to publish

 Message-->SNS Topic-->Email Subscriber

EOF
}

while getopts “t:e:m:q:?” OPTION
do
     case $OPTION in
         t)
             TOPIC=$OPTARG
             ;;
         e)
             EMAIL=$OPTARG
             ;;
         m)
             MESSAGE=$OPTARG
             ;;
         ?)
             usage
             exit 1
             ;;
     esac
done

if [[ -z $TOPIC ]] || [[ -z $EMAIL ]] || [[ -z $MESSAGE ]]
then
     usage
     exit 1
fi
function post_message_to_topic()
{
    #App Logic.....
    #App Logic ....
    #Then Publish Results!
    aws sns publish --topic-arn $arnTopic --message "$MESSAGE" || error_exit "Fail to publish to SNS Topic $arnTopic"
    ok_cont "Published message to Topic Lets wait for the mail!"
}
#Lets start...
arnTopic=$(aws sns create-topic --name $TOPIC --output text || error_exit "Failed to create an SNS Topic, Check Output")
ok_cont "Created Topic $arnTopic..."
# Let Add Subscribers!
#
aws sns subscribe --topic-arn $arnTopic --protocol email --notification-endpoint $EMAIL || error_exit "Failed to subscribe email to topic"
ok_cont "Registered Email Subscriber!"
while true ; do
    echo "Waiting for the email confirmation of the subscription please press any key when EMAIL endpoint has confirmed..."
    read -t 1 -n 1 && break
done
ok_cont "key pressed, endpoint has approved the subscription..."
#####Now lets generate a push message example
post_message_to_topic
###Cleanup is very importat !
ctrl_c
exit 0
