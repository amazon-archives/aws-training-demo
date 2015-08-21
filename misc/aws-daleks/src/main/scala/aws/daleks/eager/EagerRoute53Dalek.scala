/*
Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

    http://aws.amazon.com/apache2.0/

or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*/

package aws.daleks.eager

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.route53.AmazonRoute53Client
import scala.collection.JavaConverters._

class EagerRoute53Dalek(implicit credentials: AWSCredentialsProvider) extends Dalek {
  val r53 = new AmazonRoute53Client(credentials)

  def zones = r53.listHostedZones.getHostedZones.asScala

  def exterminate = {
    println("Exterminating Hosted Zones")
    zones.foreach { z =>
      try {
        println("** Exterminating HostedZone " + z.getName)
        // val records = r53.listResourceRecordSets(new ListResourceRecordSetsRequest().withHostedZoneId(z.getId())).getResourceRecordSets() asScala
        // records.foreach
        // TODO
      } catch {
        case e: Exception => println(s"! Failed to exterminate Zone ${z.getName()}: ${e.getMessage()}")
      }
    }

  }
}
