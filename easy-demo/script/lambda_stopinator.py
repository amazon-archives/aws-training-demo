from __future__ import print_function

import json
import boto3

# global 
nametags=['tag1','tag2']
ec2regions= ['eu-west-1', 'ap-southeast-1', 'ap-southeast-2', 'eu-central-1', 'ap-northeast-2', 'ap-northeast-1', 'us-east-1', 'sa-east-1', 'us-west-1', 'us-west-2']

print('Loading function')

def lambda_handler(event, context):
    print("Received event: " + json.dumps(event, indent=2))
    result = stopec2()
    return result  # Echo back the first key value
    
def stopec2():
    ec2 = boto3.resource('ec2');
    ret=[]
    count = 0;

    for region in ec2regions:
        target=[]
        ec2 = boto3.resource('ec2',region);
        instances = ec2.instances.all()
        for instance in instances:
            ret.append({"Region": region, "id": instance.id})
            if instance.state['Name'] != 'running': continue
            if instance.tags is not None and any(tag['Key'] == 'Name' for tag in instance.tags):
                nametag = (tag['Value'] for tag in instance.tags if tag['Key'] == 'Name').next()
                if not nametag in nametags:
                    count+=1
                    target.append(instance)
            else:
                count+=1
                target.append(instance)

        for instance in target:
                result = instance.stop()
                print (result)
    # stop all running instances we found
    print (count, "instances found")
    print (ret)
    return ret

# unused fuction
# return all region name
def getAllRegions():
    # get all regions
    ec2regions = []
    ec2client = boto3.client('ec2');
    regions = ec2client.describe_regions()
    for region in regions['Regions']:
       ec2regions.append(region['RegionName'])
    return ec2regions
    
# print (getAllRegions())
# stopec2()

'''
# lambda role policy to invoke this
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "logs:CreateLogGroup",
                "logs:CreateLogStream",
                "logs:PutLogEvents",
                "ec2:*"
            ],
            "Resource": [
                "*"
            ]
        }
    ]
}
'''