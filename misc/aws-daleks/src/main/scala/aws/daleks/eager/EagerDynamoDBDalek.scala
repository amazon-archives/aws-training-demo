/*
Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

    http://aws.amazon.com/apache2.0/

or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*/

package aws.daleks.eager

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.regions.ServiceAbbreviations
import scala.collection.JavaConverters._
import com.amazonaws.services.s3.model.{ Region => S3Region }
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.s3.model.Bucket
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import aws.daleks.util.Humid

class EagerDynamoDBDalek(implicit region: Region, credentials: AWSCredentialsProvider) extends Dalek {
  val dynamo = withRegion(new AmazonDynamoDBClient(credentials), region)

  def exterminate = {
    val tables: Seq[String] = dynamo.listTables.getTableNames asScala

    tables foreach { t =>
      info(this,s"Exterminating DyanmoDB Table ${t}")
      Humid { dynamo.deleteTable(t) }
    }

  }
}
