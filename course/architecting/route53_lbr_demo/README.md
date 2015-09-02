Route 53 Latency Based Routing Demo
===================================

This set of scripts helps to demonstrate [Latency Based Routing](http://docs.aws.amazon.com/Route53/latest/DeveloperGuide/CreatingLatencyRRSets.html) with [Route 53](http://aws.amazon.com/route53/)  DNS service

There are two ways to demo LBR, depending on the fact you are the owner of a domain managed by Route 53 or not.

Both scenario involve the use of a VPN server to simulate a connection from a different geographic location than your current network.  Instructions to setup a remote VPN server on EC2 are provided below.

##Scenario Demo #1

In this scenario, you do not need to own your own domain name.  The demo uses a fake domain name and uses ```dig``` or ```nslookup``` to query the DNS.

###Prerequisites

- An AWS account
- A VPN Connection to demonstrate a connection from another geographic location.

###Demo Script

#### On the AWS Console

- Go to the Route 53 [console]()
- Create a new Hosted Domain : example.com
	- Create a A record with Latency Based Routing turned ON (name : demo.example.com and IP address 1.1.1.1)
	- Set region to be close to you (eu-west-1 for EMEA)
	- Create a second A record with Latency Based Routing turned ON (name : demo.example.com and IP address 2.2.2.2)
	- Set region to be distant from you (us-west-1 for EMEA)
- Copy / paste one of the Route 53 DNS server name given as "Source Of Authority" for your hosted domain

#### On your local machine

- use ```nslookup``` or ```dig``` to query the DNS resolver : ```dig @<route53 server name> demo.example.com```
- Route 53 should return you the IP address closest to your location
- Now connect to a VPN server distant from your location
- use ```nslookup``` or ```dig``` to query the DNS resolver : ```dig @<route53 server name> demo.example.com```
- Route 53 should return you the IP address closest to your VPN server location	(hopefully, the second IP Address)

##Scenario demo #2

In this demo scenario, we will use rea web servers and DNS record names to demonstarte LBR.   
We will setup two simple web servers in two different geographical locations.  Web Servers main page will display the name of the region where they are running.

We will use a CFN template to create the web servers and the VPN server.

The CFN template will create DNS CNAMEs to point to the web servers and the VPN server.

###Prerequisites

- Register a hosted domain name with Route53.  You must be the owner of the domain name (you must be able to define Route 53 as SOA within your Registrar's configuration)
- A VPN Connection to demonstrate a connection from another geographic location.

###Demo Script

#### Preparation of the demo

To prepare this demo, you must run the ```cfn-route53-latency-demo-all.json``` template in two different regions (```eu-west-1``` and ```us-west-1``` for example).   The template will prompt you for the following parameters :

- the SharedSecret - to be shared between the VPN server and the VPN client
- the username 
- the password
- the hosted domain name

The script will create the following resources :

- a web server with a simple PHP page that will display the name of the region it runs into
- a VPN server with ```OpenSWAN``` and ```xl2tpd``` installed and configured
- a CNAME record pointing to the web server ```web-<region name>.<your domain name>```
- a CNAME record pointing to the VPN server ```vpn-<region-name>.<your domain name>```
	
This completes the preparation of the demo.	

#### On the AWS Console

- Go to the Route 53 [console]()
- Create two record sets on your hosted domain
	- Create a CNAME record with Latency Based Routing turned ON (for example ```demo.<your domain name>``` CNAME ```web-<region name 1>.<your domain name>```)
	- Set region to match the region of the first web server
	- Create a second CNAME record with Latency Based Routing turned ON (```demo.<your domain name>``` CNAME ```web-<region name 2>.<your domain name>```)
	- Set region to match the region of the second web server

#### On your local machine

- use your favorite browser
- Connect to ```demo.<you domain name>```, the web page should display the name of the region close to you
- Now connect to a VPN server distant from your location
- Reload the page on your browser, the web page should now display the name of the region close to your VPN server

##Scripts Description

The following scripts are provided to help you to setup this demo :

- ```cfn-route53-latency-demo-web.json``` : start and bootstrap a web server
- ```cfn-route53-latency-demo-vpn.json``` : start and bootstrap a VPN server (based on OpenSWAN and xl2tpd)
- ```cfn-route53-latency-demo-all.json``` : a wrapper template that creates both the web and the vpn servers

- ```elb-bootstrap.sh``` : the web server bootstrap script
- ```elb-examplefiles.zip``` : the web application

- ```vpn-bootstrap.sh``` : the VPN server bootstrap script

You can start the complete demo environment by running the follow AWS CLI :

(be sure to have updated ```params.json``` with proper values)
```
aws cloudformation  create-stack --stack-name route53-lbr-demo --template-url http://s3-eu-west-1.amazonaws.com/aws-emea.info/resources/route53-lbr/cfn-route53-latency-demo-all.json --parameters file://./params.json --region eu-west-1 --output text
```

I am usually doing this in two different regions

You can monitor the progress from the console or using the following AWS CLI

```
aws cloudformation describe-stacks --stack-name route53-lbr-demo --output text
```

```
aws cloudformation describe-stack-events --stack-name route53-lbr-demo --output text
```

adn be sure to delete all resources at the end of your demo

```
aws cloudformation delete --stack-name route53-lbr-demo 
```

## VPN Configuration

This section describes how to setup a VPN Server and a VPN client.

### How to setup a VPN Server?

In your remote region, run the ```cfn-route53-latency-demo-vpn.json``` template.
This will create an EC2 instance with an IPSec VPN Server (OpenSWAN).

The script will prompt you for the following parameters:

- the SharedSecret - to be shared between the VPN server and the VPN client
- the username 
- the password

These parameters must be use when you will configure the VPN client (see at the end of this document)

The script also prompts you for

- a hosted domain

Should you have a Route 53 hosted domain name, the template creates a CNAME record to point to the newly VPN Server : ```vpn-<region name>.<your hosted domain```.   
If you do not own a Route53 hosted domain, feel free to remove the two following from the CFN template before to execute it :

- the ```Hosted Domain``` from the "Parameters" section
- the "```VPNDNSRecord```" resource creation

At the end of the execution of the template, you should be able to connect to the VPN Server.

### How to setup a VPN client?

#### Mac OS X

Just configure a VPN (L2TP over IPSec) Interface in Network Preferences.

#### Windows

Configure a VPN Gateway.   

Apparently Windows does not like being behind a NAT device when doing so, so an extra step is mandatory
[http://support.microsoft.com/kb/926179/en-us](http://support.microsoft.com/kb/926179/en-us)   And reboot your machine !


#### Linux

To be done
