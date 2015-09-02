# Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


#!/bin/bash
######################
#Since Cloufromation will generate some env variabiles dynamically lets source the file
source /root/.bootstrap.cfg
##############################################################
#Functions
error_n_exit()
{
echo "`date`:ERROR $1 Exiting..." >> $LOG
/bin/cat /tmp/stderr >> $LOG
}
ok_n_cont()
{
echo "`date`:OK $1" >> $LOG
}
create_folders()
{
mkdir -p  /var/chef-solo/cache /var/chef-solo/cache/cookbooks /etc/chef /var/chef-solo/roles /var/chef-solo/check-sum
if [ "$?" == "0" ];then
  return 0
else
  return 1
fi
}
#Create Folders
create_folders
 if [ "$?" != "0" ];then
   error_n_exit "Failed to create folders"
 else
   ok_n_cont "Created Folders"
 fi
#Get Chef-Solo Artifacts from Artifacts Bucket
##
###Obtain the chef solo artifacts and copy them locally to the node
aws s3 --region $REGION cp s3://$ARTIFACTS/chef-solo-all.tar.gz "/" 2>>/tmp/s3out
 cd /
 tar xfz chef-solo-all.tar.gz || error_n_exit "could not extract chef-solo-all.tar.gz"
#Lets call OpsCode Chef-solo master install script , this will install chef-solo (omnibus)
    chmod +x /root/install.sh || error_n_exit "Could not set +x to /root/install.sh"
    source /root/install.sh 2>&1 >> $SOLOLOG
     if [ "$?" == "0" ];then
      ok_n_cont "Looks like Chef-Solo Was Installed Successfully Check $SOLOLOG for more info"
     else
      error_n_exit "Chef-solo failed to install, check $SOLOLOG for more info"
     fi
     ###Manipulating the chef attributes (we assume that cfn-init has already executed and ended successfuly)
     tb=`cat /home/ubuntu/.trailbucket`
     bk=`cat /home/ubuntu/.backupbucket`
     ak=`cat /home/ubuntu/.ak`
     sk=`cat /home/ubuntu/.sk`
     aggrprefix=`cat /home/ubuntu/.aggrprefix`
     echo "override_attributes(" >> /var/chef-solo/roles/logstash.rb
     echo "  'aws' => {" >> /var/chef-solo/roles/logstash.rb
     echo "    'bucket' => '"$tb"'," >> /var/chef-solo/roles/logstash.rb
     echo "    'backupbucket' => '"$bk"'," >> /var/chef-solo/roles/logstash.rb
     echo "    'accesskey' => '"$ak"'," >> /var/chef-solo/roles/logstash.rb
     echo "    'secretkey' => '"$sk"'," >> /var/chef-solo/roles/logstash.rb
     echo "    'region' => '"$REGION"'," >> /var/chef-solo/roles/logstash.rb
     echo "    'prefix' => '"$aggrprefix"'" >> /var/chef-solo/roles/logstash.rb
     echo "}" >> /var/chef-solo/roles/logstash.rb
     echo ")" >> /var/chef-solo/roles/logstash.rb
     ###Now lets create the log mv cron
     echo "*/1 * * * *  /root/s3mvlog.sh" >> /tmp/cronme
     chmod +x /root/s3mvlog.sh || error_n_exit "Could not set +x to /root/s3mvlog.sh"
### lets end by setting permissions
chown root.root "/etc/chef" -R
chown root.root "/var/chef-solo" -R
## Execute first chef-solo run ....
/usr/bin/chef-solo -L /var/log/solorun.log
##Setup the cronjob
echo "*/20 * * * *   /usr/bin/chef-solo -L /var/log/solorun.log" >> /tmp/cronme
#Enable the cron
crontab /tmp/cronme
#
exit 0
