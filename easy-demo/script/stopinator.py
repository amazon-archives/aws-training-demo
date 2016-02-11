import boto3
def stopec2():
    ec2 = boto3.resource('ec2');
    nametags=['watple','nextin']

    # get all regions
    ec2regions = []
    # ec2regions = ['ap-northeast-1']
    ec2client = boto3.client('ec2');
    regions = ec2client.describe_regions()

    for region in regions['Regions']:
        ec2regions.append(region['RegionName'])

    for region in ec2regions:
        print region, "check.."
        ec2 = boto3.resource('ec2',region);
        instances = ec2.instances.all()
        for instance in instances:
            state = instance.state['Name']
            print instance.id, state
            # if state <> 'running':
            #    continue
            if any(tag['Key'] == 'Name' for tag in instance.tags):
                nametag = (tag['Value'] for tag in instance.tags if tag['Key'] == 'Name').next()
                print nametag,
                if nametag in nametags:
                    print "tag ok"
                else:
                    print "not in ", nametags
                    print "Stop instance:", instance.id
                    result = instance.stop()
                    print result

stopec2()