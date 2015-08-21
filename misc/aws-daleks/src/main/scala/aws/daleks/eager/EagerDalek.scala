/*
Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

    http://aws.amazon.com/apache2.0/

or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*/

package aws.daleks.eager

import java.util.logging.Logger
import aws.daleks.util.Humid

trait Dalek {
  val logger = Logger.getLogger(classOf[Dalek].getName)
  def humidity = if (Humid.isDry) "[DRY]" else "[WET]"
	def exterminate
  def info(dalek:Dalek,msg:String) = logger.info(s"[$humidity] [$msg]")
}
