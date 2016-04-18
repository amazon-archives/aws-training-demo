#!/bin/bash
var="[Credentials]"
sed "1s/.*/$var/" /home/hadoop/.aws/config > /home/hadoop/.aws/temp
sudo mv /home/hadoop/.aws/temp /etc/boto.cfg
