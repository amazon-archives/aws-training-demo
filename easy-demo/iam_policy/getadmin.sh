#!/bin/bash
ROLE_ARN=arn:aws:iam::123456780000:role/admin_role
RET=`aws sts assume-role --role-arn $ROLE_ARN --role-session-name ec2-session --query 'Credentials.[AccessKeyId, SecretAccessKey, SessionToken]' --output=text`
export AWS_ACCESS_KEY_ID=`echo $RET | cut -f1`
export AWS_SECRET_ACCESS_KEY=`echo $RET | cut -f2`
export AWS_SESSION_TOKEN=`echo $RET | cut -f3`
