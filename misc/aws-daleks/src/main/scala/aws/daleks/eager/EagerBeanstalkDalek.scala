/*
Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

    http://aws.amazon.com/apache2.0/

or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*/

package aws.daleks.eager

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient
import com.amazonaws.regions.Region
import scala.collection.JavaConverters._
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus
import com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest
import aws.daleks.util.Humid

class EagerBeanstalkDalek(implicit region: Region, credentials: AWSCredentialsProvider) extends Dalek {
  val beanstalk = withRegion(new AWSElasticBeanstalkClient(credentials), region)

  def exterminate = {
    val TERMINATED = EnvironmentStatus.Terminated.toString()
    val envs = beanstalk.describeEnvironments().getEnvironments().asScala filter { e =>
      !TERMINATED.equalsIgnoreCase(e.getStatus())
    }

    val apps = try {
      beanstalk.describeApplications.getApplications asScala
    } catch {
      case e: Exception => {
        println("Could not fectch beanstalk applications: " + e.getMessage());
        List.empty
      }
    }

    envs foreach exterminateEnv

    apps foreach exterminateApp
  }

  def exterminateEnv(env: EnvironmentDescription) =
    try {
      val envName = env.getEnvironmentName()
      println(s"** Exterminating Beanstalk Environment ${envName} [${env.getStatus()} ] ")
      Humid {
      beanstalk.terminateEnvironment(new TerminateEnvironmentRequest()
        .withEnvironmentName(envName)
        .withTerminateResources(true))
      }
    } catch {
      case e: Exception => println(s"! Failed to exterminate Beanstalk Environment ${env.getEnvironmentName()} [id: ${env.getEnvironmentId} ]: ${e.getMessage()}");
    }

  def exterminateApp(app: ApplicationDescription) =
    try {
      println("** Exterminating Beanstalk Application " + app.getApplicationName())
      Humid {
      beanstalk.deleteApplication(new DeleteApplicationRequest().withApplicationName(app.getApplicationName()))
      }
    } catch {
      case e: Exception => println(s"! Failed to exterminate Beanstalk Application ${app.getApplicationName()}: ${e.getMessage()}")
    }
}
