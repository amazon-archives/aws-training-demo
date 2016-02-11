from __future__ import print_function

import json
import boto3

print('Loading function')

def lambda_handler(event, context):
    #print("Received event: " + json.dumps(event, indent=2))
    # print("value1 = " + event['key1'])
    # print("value2 = " + event['key2'])
    # print("value3 = " + event['key3'])
    result = stopec2()
    return result  # Echo back the first key value
    #raise Exception('Something went wrong')
    
def stopec2():
    ec2 = boto3.resource('ec2');
    nametags=['watple','nextin']
    result=[];

    # get all regions
    ec2regions = []
    # ec2regions = ['ap-northeast-1']
    ec2client = boto3.client('ec2');
    regions = ec2client.describe_regions()

    for region in regions['Regions']:
        ec2regions.append(region['RegionName'])

    for region in ec2regions:
        print(region, "check..")
        ec2 = boto3.resource('ec2',region);
        instances = ec2.instances.all()
        for instance in instances:
            state = instance.state['Name']
            print(instance.id, state)
            # if state <> 'running':
            #    continue
            if any(tag['Key'] == 'Name' for tag in instance.tags):
                nametag = (tag['Value'] for tag in instance.tags if tag['Key'] == 'Name').next()
                if nametag in nametags:
                    print(nametag, "tag ok")
                else:
                    print(nametag, " not in", nametags)
                    result.append({"Region":region, "id": instance.id})
                    result = instance.stop()
                    print(result)
    