SGFinder
========

SGFinder will list (find) and optionally cleanup unused security groups in your account accross all regions.

Usage
-----
Review the following parameters:
* ```IncludeDefault``` should the default security groups be included or not
* ```DeleteEmpty``` should empty groups be removed
* ```AccessKey``` & ```SecretKey``` configure your keys to enable access. A security [best practice](http://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html#use-roles-with-ec2) is to use roles instead, to use roles simple removed the ```aws_access_key_id``` and ```aws_secret_access_key``` parameters on line 45.

Run ```findSG.py``` 