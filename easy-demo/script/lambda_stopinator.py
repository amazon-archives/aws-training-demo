from __future__ import print_function

import json
import boto3

print('Loading function')

def lambda_handler(event, context):
    result = stopec2()
    return result  
    
def stopec2():
    ec2 = boto3.resource('ec2');
    nametags=['watple','nextin']
    ec2regions= ['eu-west-1', 'ap-southeast-1', 'ap-southeast-2', 'eu-central-1', 'ap-northeast-2', 'ap-northeast-1', 'us-east-1', 'sa-east-1', 'us-west-1', 'us-west-2']
    ret=[]

    for region in ec2regions:
        target=[]
        ec2 = boto3.resource('ec2',region);
        instances = ec2.instances.all()
        for instance in instances:
            if instance.tags is not None and any(tag['Key'] == 'Name' for tag in instance.tags):
                nametag = (tag['Value'] for tag in instance.tags if tag['Key'] == 'Name').next()
                if not nametag in nametags:
                    target.append(instance)
                    ret.append({"Region": region, "id": instance.id})
            else:
                target.append(instance)
                ret.append({"Region": region, "id": instance.id})

        # stop all running instances we found
        for instance in target:
            if instance.state['Name'] == 'running':
                result = instance.stop()
                print (result)
    return ret
    