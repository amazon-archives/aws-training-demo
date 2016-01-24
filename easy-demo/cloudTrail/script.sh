#!/bin/bash
ACCOUNTNUM=$1
AMI=ami-383c1956
aws iam create-policy --policy-name 'cwlogs' --policy-document file://cwlogs-policy.json
aws iam create-role --role-name 'ec2-cwlogs' --assume-role-policy-document file://ec2-role-trust-policy.json
aws iam attach-role-policy --policy-arn arn:aws:iam::$ACCOUNTNUM:policy/cwlogs --role-name ec2-cwlogs
aws iam create-instance-profile --instance-profile-name cwlogs
aws iam add-role-to-instance-profile --instance-profile-name cwlogs --role-name ec2-cwlogs
aws ec2 run-instances --image-id ami-383c1956 --count 1 --iam-instance-profile Name=cwlogs --instance-type t2.micro --key-name wpl-jap --user-data file://userdata.txt --security-group-ids sg-f025c094