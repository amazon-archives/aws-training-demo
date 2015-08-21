/*
Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

    http://aws.amazon.com/apache2.0/

or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*/

package aws.daleks.eager

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.regions.Region
import scala.collection.JavaConverters._
import com.amazonaws.services.ec2.model.StopInstancesRequest
import com.amazonaws.services.ec2.model.TerminateInstancesRequest
import com.amazonaws.services.ec2.model.Instance
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient
import com.amazonaws.services.elasticloadbalancing.model.DeleteLoadBalancerRequest
import com.amazonaws.services.ec2.model.DeleteVolumeRequest
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest
import com.amazonaws.services.ec2.model.RevokeSecurityGroupEgressRequest
import com.amazonaws.services.ec2.model.DeleteKeyPairRequest
import com.amazonaws.services.ec2.model.InstanceState
import com.amazonaws.services.ec2.model.DeregisterImageRequest
import com.amazonaws.services.ec2.model.DescribeImagesRequest
import aws.daleks.util.Humid

class EagerEC2Dalek(implicit region: Region, credentials: AWSCredentialsProvider) extends Dalek {
  val ec2 = withRegion(new AmazonEC2Client(credentials), region)
  val elb = withRegion(new AmazonElasticLoadBalancingClient(credentials), region)

  def TerminateOrStop(i: Instance) = try {
    info(this, s"** Exterminating EC2 Instance ${i.getInstanceId} [${i.getState.getName}] on region ${region}")
    Humid {
      ec2.terminateInstances(new TerminateInstancesRequest().withInstanceIds(i.getInstanceId))
    }
  } catch {
    case e: Exception => {
      info(this, "! Failed to terminate EC2 Instance" + i.getInstanceId())
      if ("Running".equalsIgnoreCase(i.getState.getName())) {
        info(this, s"** Stopping EC2 Instance ${i.getInstanceId} [${i.getState.getName}] on region ${region}")
        ec2.stopInstances(new StopInstancesRequest().withInstanceIds(i.getInstanceId()))
      }
    }
  }

  def exterminateKeypairs = {
    val keypairs = ec2.describeKeyPairs().getKeyPairs().asScala
    keypairs foreach { k =>
      try {
        info(this, "** Exterminating KeyPair " + k.getKeyName())
        Humid {
          ec2.deleteKeyPair(new DeleteKeyPairRequest(k.getKeyName()))
        }
      } catch {
        case e: Exception => info(this, s"! Failed to exterminate KeyPair ${k.getKeyName()}: ${e.getMessage()}")
      }
    }
  }

  def exterminateInstances = {
    val reservations = ec2.describeInstances.getReservations asScala
    val instances = reservations
      .flatMap { r => r.getInstances asScala }
      .filter { i => !i.getState.getName().equalsIgnoreCase("terminated") }

    instances foreach TerminateOrStop
  }

  //TODO: Recurse dependencies
  def exterminateSecurityGroups = {
    val secGroups = ec2.describeSecurityGroups().getSecurityGroups().asScala.filter(_.getGroupName != "default")
    secGroups foreach { sg =>
      try {

        val ingress = sg.getIpPermissions
        info(this, s"** Revoking [${ingress.size}] ingress rules for [${sg.getGroupId}]")
        Humid {
          ec2.revokeSecurityGroupIngress(
            new RevokeSecurityGroupIngressRequest()
              .withGroupId(sg.getGroupId())
              .withIpPermissions(ingress))
        }

        val egress = sg.getIpPermissionsEgress
        info(this, s"** Revoking [${egress.size}] egress rules for [${sg.getGroupId}]")
        Humid {
          ec2.revokeSecurityGroupEgress(
            new RevokeSecurityGroupEgressRequest()
              //.withGroupId(sg.getGroupId())
              .withIpPermissions(egress))
        }

      } catch {
        case e: Exception => info(this, s"! Failed to clean Security Group ${sg.getGroupId()}: ${e.getMessage()}")
      }
    }

    secGroups.foreach { sg =>
      try {
        info(this, "** Exterminating Security Group " + sg.getGroupId())
        Humid { ec2.deleteSecurityGroup(new DeleteSecurityGroupRequest().withGroupId(sg.getGroupId())) }
      } catch {
        case e: Exception => info(this, s"! Failed to exterminate Security Group ${sg.getGroupId()}: ${e.getMessage()}")
      }

    }
  }

  def exterminateVolumes = {
    val volumes = ec2.describeVolumes.getVolumes.asScala.filter {
      v => !"in-use".equals(v.getState)
    }
    volumes filter { "in-use" != _.getState } foreach { v =>
      info(this, s"** Exterminating Volume ${v.getVolumeId}[${v.getState}]")
      Humid { ec2.deleteVolume(new DeleteVolumeRequest().withVolumeId(v.getVolumeId)) }
    }
  }

  def exterminateELBs = {
    val elbs = elb.describeLoadBalancers().getLoadBalancerDescriptions().asScala
    elbs foreach { lb =>
      try {
        info(this, "** Exterminating Elastic Load Balancer " + lb.getLoadBalancerName())
        Humid { elb.deleteLoadBalancer(new DeleteLoadBalancerRequest().withLoadBalancerName(lb.getLoadBalancerName())) }
      } catch {
        case e: Exception => info(this, s"! Failed to exterminate Load Balancer ${lb.getLoadBalancerName()}: ${e.getMessage()}")
      }
    }
  }

  def exterminateAMIs = {
    val amis = ec2.describeImages(new DescribeImagesRequest().withOwners("self")).getImages().asScala
    amis foreach { ami =>
      try {
        info(this, s"** Exterminating Image [${ami.getImageId}] [${ami.getName}]")
        Humid {
          ec2.deregisterImage(new DeregisterImageRequest().withImageId(ami.getImageId()))
        }
      } catch {
        case e: Exception => info(this, s"! Failed to exterminate Image [${ami.getImageId()}]: ${e.getMessage()}")
      }

    }
  }

  def exterminate = {
    exterminateInstances
    exterminateKeypairs
    exterminateVolumes
    exterminateELBs
    exterminateSecurityGroups
    exterminateAMIs
  }
}
