# Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


#!/bin/bash -x

# Please define your own values for those variables
# these will be injected into that script by the CFN template bootstrap script
#IPSEC_PSK=SharedSecret
#VPN_USER=username
#VPN_PASSWORD=password

# Those two variables will be found automatically
PRIVATE_IP=`wget -q -O - 'http://instance-data/latest/meta-data/local-ipv4'`

#the following does not work in VPC
#PUBLIC_IP=`wget -q -O - 'http://instance-data/latest/meta-data/public-ipv4'`
#
# use http://169.254.169.254/latest/meta-data/network/interfaces/macs/06:79:3f:b2:49:20/ipv4-associations/ instead but depends on mac address :-(
#
PUBLIC_IP=`wget -q -O - 'checkip.amazonaws.com'`


# send std out to log file
exec &>> /home/ec2-user/bootstrap-vpn.log

function error_exit() {
    echo "{\"Reason\": \"$1\"}"
    exit $2
}

RETURN_CODE=$(/usr/bin/yum install -y --enablerepo=epel openswan xl2tpd)
if [ $RETURN_CODE -ne 0 ]
then
    error_exit "yum install openswan xl2tpd failed" $RETURN_CODE
fi

cat > /etc/ipsec.conf <<EOF
version 2.0

config setup
	dumpdir=/var/run/pluto/
	nat_traversal=yes
	virtual_private=%v4:10.0.0.0/8,%v4:192.168.0.0/16,%v4:172.16.0.0/12,%v4:25.0.0.0/8,%v6:fd00::/8,%v6:fe80::/10
	oe=off
	protostack=netkey
	nhelpers=0
	interfaces=%defaultroute

conn vpnpsk
	auto=add
	left=$PRIVATE_IP
	leftid=$PUBLIC_IP
	leftsubnet=$PRIVATE_IP/32
	leftnexthop=%defaultroute
	leftprotoport=17/1701
	rightprotoport=17/%any
	right=%any
	rightsubnetwithin=0.0.0.0/0
	forceencaps=yes
	authby=secret
	pfs=no
	type=transport
	auth=esp
	ike=3des-sha1
	phase2alg=3des-sha1
	dpddelay=30
	dpdtimeout=120
	dpdaction=clear
EOF

cat > /etc/ipsec.secrets <<EOF
$PUBLIC_IP %any : PSK "$IPSEC_PSK"
EOF

cat > /etc/xl2tpd/xl2tpd.conf <<EOF
[global]
port = 1701

;debug avp = yes
;debug network = yes
;debug state = yes
;debug tunnel = yes

[lns default]
ip range = 192.168.42.10-192.168.42.250
local ip = 192.168.42.1
require chap = yes
refuse pap = yes
require authentication = yes
name = l2tpd
;ppp debug = yes
pppoptfile = /etc/ppp/options.xl2tpd
length bit = yes
EOF

cat > /etc/ppp/options.xl2tpd <<EOF
ipcp-accept-local
ipcp-accept-remote
ms-dns 8.8.8.8
ms-dns 8.8.4.4
noccp
auth
crtscts
idle 1800
mtu 1280
mru 1280
lock
connect-delay 5000
EOF

cat > /etc/ppp/chap-secrets <<EOF
# Secrets for authentication using CHAP
# client server secret IP addresses

$VPN_USER l2tpd $VPN_PASSWORD *
EOF

iptables -t nat -A POSTROUTING -s 192.168.42.0/24 -o eth0 -j MASQUERADE
echo 1 > /proc/sys/net/ipv4/ip_forward

iptables-save > /etc/iptables.rules

mkdir -p /etc/network/if-pre-up.d
cat > /etc/network/if-pre-up.d/iptablesload <<EOF
#!/bin/sh
iptables-restore < /etc/iptables.rules
echo 1 > /proc/sys/net/ipv4/ip_forward
exit 0
EOF

chkconfig ipsec on
chkconfig xl2tpd on

RETURN_CODE=$(service ipsec start)
if [ $RETURN_CODE -ne 0 ]
then
    error_exit "Can not start ipsec" $RETURN_CODE
fi

RETURN_CODE=$(service xl2tpd start)
if [ $RETURN_CODE -ne 0 ]
then
    error_exit "Can not start xl2tpd" $RETURN_CODE
fi
