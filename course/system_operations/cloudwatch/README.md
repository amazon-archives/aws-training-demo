Simple Custom Cloudwatch Statistics Set Example
===============================================
Very simple but useful! example to show students how east it to publish a custom metric from statistics set type
more info here: http://docs.aws.amazon.com/AmazonCloudWatch/latest/DeveloperGuide/publishingMetrics.html#publishingDataPoints1   #Publish Statistic Sets

Custom Metrics costs can be high , to try and save costs customer can sample few business data points and aggregate them on the client side
Then Publish them as "Already Aggregated Data Points" for example Say you need to sample the performance of your webapp which consists of 
3,4 Counters such as : Average Time to Load the Page , Average Database Access ... you could simply get those counters and perform 1st level
Aggregation which consists on sum/average/min/max then publish it to cloudwatch as Single Metric per instance (Using instanceID as dimension

That is about it see you next time Trainers! 

To test it just:

- Spin up your favorite nix flavour (I Use ubuntu)
- You will need the aws cli tools installed (for ubuntu you need to install it)
- You will need an IAM Role and a policy to allow the ec2 instance to post the metrics , the below will do the trick
```
  {
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "Stmt1396006753000",
      "Effect": "Allow",
      "Action": [
        "cloudwatch:GetMetricStatistics",
        "cloudwatch:ListMetrics",
        "cloudwatch:PutMetricAlarm",
        "cloudwatch:PutMetricData"
      ],
      "Resource": [
        "*"
      ]
    }
  ]
}
```
- Install the sysstat binary, apt-get install sysstat , yum install sysstat
- create a crontab from the ubuntu/ec2-user and execute the script every 1/5 whatever minutes you wish

Enjoy! 

