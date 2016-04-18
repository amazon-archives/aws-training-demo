#!/usr/bin/python

# Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


# findSG.py:	Prints out all security groups across regions and the amount of instances using them
#		(and optionally removes them), including/excluding the "default" ones too 


# Config part
## IncludeDefault:	Change this to include/exclude the "default" SG and set your access+secrets
## DeleteEmpty:		Remove the empty SG or not?
## Access/Secret:	Only there because of testing on my box and not an instance with a role, remove if you put 'em on an EC2
IncludeDefault	= False
DeleteEmpty	= True
AccessKey	= 'PLEASE_CHANGE'
SecretKey	= 'BOTH_OF_US'


# You know...
import boto
ec2 = boto.connect_ec2()
rgs = boto.ec2.regions()
tcn = 0
rcn = 0
dcn = 0


# Go over all regions
for rg in rgs:

	# Skipping China & GovCloud
	if rg.name.startswith( 'cn-' ) or rg.name.startswith( "us-gov" ):
		continue
	

	# Header + connect + get the collection of SG
	print 'Region: %s\n--------------------'  % (rg.name)
	sgs = boto.ec2.connect_to_region( rg.name, aws_access_key_id = AccessKey, aws_secret_access_key = SecretKey ).get_all_security_groups()
	

	# SG iterator
	for sg in sgs:
		
		# Did you ask for the default? :|
		if IncludeDefault == False and sg.name == 'default':
			continue
		
		# "Table" + print
		if rcn == 0:
			print '%-68s\t%-12s' % ("Security Group Name:", "Used by [instance count]:")
		print '%-68s\t%-12s' % (sg.name, len(sg.instances())),
		
		# Cleanup sir?
		if( len(sg.instances()) == 0 and DeleteEmpty ):
			try:
				sg.delete( dry_run = False )
				break
			except boto.exception.EC2ResponseError as e:
				print "[shhh, it's a dry run]",
			
			print "\tRemoved",
			dcn += 1
		
		print ''	
		rcn += 1

	
	# Region sum
	if rcn == 0:
		print '\nNo unused security groups were found',
	else:
		print '\n%d unused security groups were found' % (rcn),

	print 'in %s\n' % (rg.name)
	tcn += rcn
	rcn = 0


# General sum
if tcn == 0:
	print '\n\nSummary: Great job! no unused security groups were found!'
else:
	print '\n\nSummary: %d unused security groups were found' % (tcn)

# Removal sum
if DeleteEmpty:
	print '\t %d security groups were removed\n' % (dcn)
