Summary
=======
Bootstrap your instance automatically with chef-solo, The purpose is to demonstrate to students how *relatively* easy it is to use automation tools such as chef-solo to automate deployments on AWS.
By all means this mini framework does not "compete" with a fully blown chef-server / OpsWorks , Its sole purpose is to provide a simple yet powerful first step into automating instance deployments @AWS Using chef-solo


Components
==========

* Admin Instance - The instance that will orchestrate the instance bootstrapping
* Bootstrapped EC2 Instance - The instance that will be launched as part of the process
* Chef-solo - The chef-client that will be executed on every Bootstrapped EC2 Instance, Executing cookbooks
* Artifcats Repo - S3 Bucket that will be created by Cloudformation, Staging the artifcats: Chef Cookbooks , Bootstrap Scripts...
* Instance Role/Instance Profile - Allow the Bootstrapped EC2 instance to make Authenticated API calls to the S3 bucket 
* Cookbooks - Contains the Recipes that will be executed on the bootstrapped instance


Programs
========

newInstance.sh - Once the admin instance is up, this script is will be used to bootstrap EC2 Instances with chef-solo

bootstrap.sh - newInstance.sh will inject userdata to The bootstrapped Instance, the userdata will download&execute bootstrap.sh
               Which in turn will install chef-solo and execute the cookbooks...

solocron.sh - A cron job script running on The bootstrapped instance (every 20 mins) , the script will track the changes to the chef-solo
              configurations stored on s3

*Step 1 - Create the Demo Environments*
---------------------------------------

For the lazy:

- Download the chef-solo-admin-instance.json cloudformation template
- Choose the region you wish to deploy 
- Use cloudformation to deploy

The above CFN template will create an Ubuntu Admin Instance, and will stage all artifacts under /home/ubuntu, Just login
And execute newInstance.sh to bootstrap the ec2 instance with chef-solo (see next step for more details)


*Step 2 - Bootstrap the Instance*
---------------------------------

****Important: I assume that you launch the instance into a pre-existing VPC , & Into a valid existing PUBLIC SUBNET that automatically assigns public IP
               to the newly launched instance****

- Login to the admin instance
- Change dir to /home/ubuntu
- As the ubuntu user: execute the newInstance.sh , provide all the needed arguments to the script.
  Several Argument Values shall be taken from the cloudformation's stack output (see below for more details)

Usage:

usage:  ./newInstance.sh -a AMI-ID -s securityGroupID -k KeyPairName -i InstanceSize -n VPCsubnetID -m IAM-Instance-Profile(Arn=value) -c ChefRole

Currently ChefRole can be "base" or "web"

Example:

Start an EC2 Instance and have chef deploy it as an apache webserver:

 ./newInstance.sh -a ami-423c0a5f -s sg-xxxxxxxx -k xxxxxxxx -i m3.medium -n subnet-xxxxxxxx -m Arn=arn:aws:iam::xxxxxxxxxxxxxxxx:instance-profile/s3-chef-solo -c web
 
 1. *securityGroupID*: The bootstrapped instance Security Group Id, See the cloudformation output for the value : ChefSoloInstanceSecurityGroupID
 2. *KeyPairName*: Your keypair name, the public part will be pushed to the instance
 3. *InstanceSize*: Any instance size
 4. *VPCsubnetID*: The VPC Subnet Id to start the instance into, Subnet MUST BE PUBLIC and auto assign public IPs unless you plan to use VPN
 5. *IAM-Instance-Profile*: The IAM Role that will be attached to The bootstrapped instance, See the cloudformation output for the value of: ChefSoloInstanceProfileArn
 6. *AMI-ID*:  Only ubuntu based AMI's are supported !
 7. *ChefRole*: Supported Roles are:  "base" or "web"

- Login to the instance after a few minutes the chef-solo web role should have installed apache2 automatically!, browse to the ec2 instance public ip

Troubleshooting
===============

 The first step to troubleshoot is logs , I made sure that all the bootstrap process will be logged for any possible error:
 
 SSH login to the bootstrapped instance and examine the following logs in that order:
   
 - /var/log/cloud-init-output ---> Very important log , the user-data execute stdout will be logged here so if for some reason
   bootstrap.sh could not be downloaded from s3 the error will appear here
 - /var/log/bootstrap.log ---> This script will install the chef-client ,create folders  , install AWS CLI...
 - /var/log/solorun.log ---> This logs means that chef-solo has executed , used to troubleshoot cookbook installation issues
 
 Tip: When deploying the CFN Template DISABLE ROLLBACK this will enable you to login to the admin instance and investigate if needed

Author
======

Kobi Biton:  kobibito@amazon.lu

Do not hesitate to contact me for feedback / questions


