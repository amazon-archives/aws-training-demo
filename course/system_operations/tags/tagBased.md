Summary
=======

Simple program which loops the describe ec2 API seeking for key:value pair and act upon: stop/terminate in addition will send SNS Messages to pre-defined topic

Name
====
tagBased.sh

Goal
====
Stop or Terminate EC2 resources based on key:value Tag Pair , currently only support instance as resource type , future will also suppot EBS volumes

PreReq
======

  * AWS Python CLI tools
  * Access / Secert Key with IAM Policy that at mimimum needs permissions to: Describe/Stop/Terminate Instanceas/Tags and optionally send SNS message to topic

Usage
=====
	$ usage: ./tagBased.sh -t ResourceType (instance / volume)  -a APIToExecute (Stop/Terminate) -k (TagKeyName) -v (TagValue) -s [SNSTopicARN-NAME]
	$ This script will Filter Tags based on resource type and then will loop all tag key:value pairs , based on a condition will perform API Operations (Currently Supports Only Terminate and Run Instances API)
	$ OPTIONS:
	$   -t      Mandatory: Resource Type, Currently only "instance" is supported!
	$   -a      Mandatory: API To execute aginst the instance Stop/Terminate
	$   -k      Mandatory: The Tag Key Name
	$   -v      Mandatory: The Tag Value
	$   -s      OPTIONAL:  SNSARNTopicName
	$ describe-tags with Filter --> Sort --> Loop based on specific Tag --> Perform Action Based On Tag Name  --> Send message to SNS Topic
