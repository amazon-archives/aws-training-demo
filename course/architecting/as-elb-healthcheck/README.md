#AutoScaling ELB HealthCheck Demo

This scripts demonstrate AutoScaling healthcheck at the ELB level.  
In this scenario, the Auto Scaling service will probe the ELB about the health of the instance.  It will replace any instance that fails to answer to ELB heatlthcheck during the period configured at ELB level.

For web application, this scenario is better than traditional AutoScaling healthcheck at instance level since it allows to detect application level failures.

## Demo Script

### Prerequisites

This scenario demo script requires that

- you have installed the AWS CLI package on the machine you will use for the demo
- AWS CLI is correctly configured (AWS_CONFIG_FILE environment variable pointing to a config file)
- the command below are prepared for eu-west-1 region, you will need to adapat it to your region name and availability zones names

The AWS CLI configuration file must look like this :

```
[default]
aws_access_key_id = AKIA...
aws_secret_access_key = fFVT...
region = eu-west-1
```

### Demo

#### Create the demo environment 

Create a load balancer

```aws elb create-load-balancer --availability-zones eu-west-1a eu-west-1b --load-balancer-name asdemo --region eu-west-1 --listeners protocol=http,load_balancer_port=80,instance_protocol=http,instance_port=80```

Configure ELB's healthcheck

```aws elb configure-health-check --load-balancer-name asdemo --health-check target=HTTP:80/index.php,interval=10,timeout=5,unhealthy_threshold=2,healthy_threshold=2```

Create an AutoScaling launch configuration (Might need to chang the AMI id)

```aws autoscaling  create-launch-configuration --image-id ami-c7c0d6b3 --instance-type t1.micro --key-name sst-aws --launch-configuration-name asdemo-lc --region eu-west-1 --security-groups web --user-data file://./web-bootstrap.sh```


Create an AutoScaling group (notice ```--health-check-type ELB ``` parameter)

```aws autoscaling create-auto-scaling-group --auto-scaling-group-name asdemo --availability-zones eu-west-1a eu-west-1b --desired-capacity 2 --min-size 2 --max-size 2 --health-check-grace-period 30 --health-check-type ELB --launch-configuration-name asdemo-lc --load-balancer-names asdemo --region eu-west-1```

Check AutoScaling is OK

```aws autoscaling describe-auto-scaling-instances```


#### SSH connect to one of the machine and stop the web server

/etc/init.d/httpd stop


#### Sit down, relax and watch AS in action

After a few seconds, you should see one of the two instances marked as ```UNHEALTHY```


```
$ aws autoscaling describe-auto-scaling-instances
[
    {
        "AvailabilityZone": "eu-west-1b",
        "InstanceId": "i-704d4b3d",
        "AutoScalingGroupName": "test-as",
        "HealthStatus": "HEALTHY",
        "LifecycleState": "InService",
        "LaunchConfigurationName": "asdemo-lc"
    },
    {
        "AvailabilityZone": "eu-west-1a",
        "InstanceId": "i-764d4b3b",
        "AutoScalingGroupName": "test-as",
        "HealthStatus": "UNHEALTHY",
        "LifecycleState": "Terminating",
        "LaunchConfigurationName": "asdemo-lc"
    }
]
```

### Cleanup

Delete all resources created :

```aws autoscaling update-auto-scaling-group --min-size 0 --max-size 0 --auto-scaling-group-name asdemo```

```aws autoscaling  delete-auto-scaling-group --auto-scaling-group-name asdemo```

```aws autoscaling  delete-launch-configuration --launch-configuration-name asdemo-lc```

```aws elb delete-load-balancer --load-balancer-name asdemo```

##TODO

Maybe it would benice to have a CFN template to setup this demo
