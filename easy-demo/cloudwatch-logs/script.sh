#!/bin/bash
ACCOUNTNUM=$1
AMI=ami-383c1956
aws iam create-policy --policy-name 'cwlogs' --policy-document file://cwlogs-policy.json
aws iam create-role --role-name 'ec2-cwlogs' --assume-role-policy-document file://ec2-role-trust-policy.json
aws iam attach-role-policy --policy-arn arn:aws:iam::525434708078:policy/cwlogs --role-name ec2-cwlogs
aws iam list-roles --query Roles[*].RoleName
aws iam create-instance-profile --instance-profile-name cwlogs
aws iam add-role-to-instance-profile --instance-profile-name cwlogs --role-name ec2-cwlogs
aws ec2 run-instances --image-id ami-249b554a --count 1 --iam-instance-profile Name=cwlogs --instance-type t2.micro --key-name nextin-icn --user-data file://userdata.txt --security-group-ids sg-b4b158dd --region=ap-northeast-2
aws ec2 create-tags --resources <instance-id> --tags 'Key=Name,Value=WebServer'