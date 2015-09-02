/*
Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

    http://aws.amazon.com/apache2.0/

or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*/

package aws.daleks.eager

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Region
import scala.collection.JavaConverters._
import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.sqs.model.DeleteQueueRequest
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient
import com.amazonaws.services.cloudformation.model.DeleteStackRequest
import aws.daleks.util.Humid

class EagerCloudFormationDalek(implicit region: Region, credentials: AWSCredentialsProvider) extends Dalek {
  val cloudformation = withRegion(new AmazonCloudFormationClient(credentials), region)

  def exterminate = {
    val stacks = cloudformation.describeStacks.getStacks asScala

    stacks foreach { stack =>
      try {
        info(this,s"** Exterminating CloudFormation Stack " + stack.getStackName())
        Humid {
        cloudformation.deleteStack(new DeleteStackRequest().withStackName(stack.getStackName()))
        }
      } catch {
        case e: Exception => println(s"! Failed to exterminate Beanstalk Application ${stack.getStackName}: ${e.getMessage()}")
      }
    }
  }
}
