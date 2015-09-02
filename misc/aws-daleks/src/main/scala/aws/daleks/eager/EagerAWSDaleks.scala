/*
Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

    http://aws.amazon.com/apache2.0/

or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*/

package aws.daleks.eager

import com.amazonaws.regions.Regions
import com.amazonaws.regions.Regions._
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.profile.ProfileCredentialsProvider

object EagerAWSDaleks extends App {
  println("EXTERMINATE!")

  def findArg(arg:String):Option[String] = {
    val i = args.indexOf(s"-$arg")
    if ( i >= 0)
      Option(args(i+1))
    else None
  }

  val profile = findArg("profile")

  implicit val credentials = profile match {
    case Some(prf) => new ProfileCredentialsProvider(prf)
    case None => new DefaultAWSCredentialsProviderChain
  }

  val excludedRegions = List(GovCloud,CN_NORTH_1)
  val regions = Regions.values diff excludedRegions

  println(s"Exterminating regions [${regions.mkString(",")}]")

  val globals = List(
    new EagerRoute53Dalek(),
    new EagerIAMDalek())

  val regionals = regions
    .map { Region.getRegion(_) }
    .par
    .foreach { implicit region =>
      println("Preparing extermination of region ["+region+"]")
      List(new EagerS3Dalek,
        new EagerBeanstalkDalek,
        new EagerCloudFormationDalek,
        new EagerDynamoDBDalek,
        new EagerElastiCacheDalek,
        new EagerEMRDalek,
        new EagerRDSDalek,
        new EagerS3Dalek,
        new EagerSNSDalek,
        new EagerSQSDalek,
        new EagerEC2Dalek) foreach {_.exterminate}
    }

  globals foreach { _.exterminate }

  println("EXTERMINATE!")

}
