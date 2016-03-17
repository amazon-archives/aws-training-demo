#!/bin/bash
SUBNET=<subnet-id>
SG=<sg-id>
KEY=<keypair>

AMI=`curl -s -w '\n' http://169.254.169.254/latest/meta-data/ami-id`

# create ec2 instance
RESULT=`aws ec2 run-instances --image-id $AMI --user-data file://userdata.txt --key-name $KEY \
--security-group-ids $SG --instance-type t2.nano --subnet-id $SUBNET --associate-public-ip-address \
--query 'Instances[*].[InstanceId,PrivateIpAddress]' --output=text`

ID=`echo $RESULT | cut -d ' ' -f1`
IP=`echo $RESULT | cut -d ' ' -f2`

echo ip: $IP, instance-id: $ID

#set tag value to instance
aws ec2 create-tags --resources $ID --tags "Key=Name,Value=temp"
