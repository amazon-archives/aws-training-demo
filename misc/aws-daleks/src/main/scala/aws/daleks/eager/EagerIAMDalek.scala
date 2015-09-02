/*
Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

    http://aws.amazon.com/apache2.0/

or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*/

package aws.daleks.eager

import java.io.InputStreamReader
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient
import java.util.Properties
import com.amazonaws.auth.AWSCredentialsProvider
import scala.collection.JavaConverters._
import com.amazonaws.services.identitymanagement.model.DeleteRoleRequest
import com.amazonaws.services.identitymanagement.model.DeleteGroupRequest
import com.amazonaws.services.identitymanagement.model.ListGroupsForUserRequest
import com.amazonaws.services.identitymanagement.model.ListAccessKeysRequest
import com.amazonaws.services.identitymanagement.model.ListUserPoliciesRequest
import com.amazonaws.services.identitymanagement.model.DeleteAccessKeyRequest
import com.amazonaws.services.identitymanagement.model.ListGroupPoliciesRequest
import com.amazonaws.services.identitymanagement.model.DeleteUserRequest
import com.amazonaws.services.identitymanagement.model.DeleteUserPolicyRequest
import com.amazonaws.services.identitymanagement.model.RemoveUserFromGroupRequest
import com.amazonaws.services.identitymanagement.model.DeleteGroupPolicyRequest
import com.amazonaws.services.identitymanagement.model.DeleteLoginProfileRequest
import com.amazonaws.services.identitymanagement.model.GetLoginProfileRequest
import com.amazonaws.services.identitymanagement.model.DeleteInstanceProfileRequest
import com.amazonaws.services.identitymanagement.model.RemoveRoleFromInstanceProfileRequest
import com.amazonaws.services.identitymanagement.model.ListInstanceProfilesForRoleRequest
import com.amazonaws.services.identitymanagement.model.GetRolePolicyRequest
import com.amazonaws.services.identitymanagement.model.ListRolePoliciesRequest
import com.amazonaws.services.identitymanagement.model.DeleteRolePolicyRequest
import aws.daleks.util.Humid

class EagerIAMDalek(implicit credentials: AWSCredentialsProvider) extends Dalek {
  lazy val key = {
    val p = new Properties
    val stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("AwsCredentials.properties")
    val reader = new InputStreamReader(stream)
    p.load(reader)
    val k = p.getProperty("accessKey")
    k
  }
  val iam = new AmazonIdentityManagementClient(credentials);

  def users = iam.listUsers().getUsers().asScala filter { u =>
    !"dalek".equals(u.getUserName())
  }

  def roles = iam.listRoles().getRoles() asScala
  def groups = iam.listGroups().getGroups() asScala

  def exterminate = {

    info(this, "Exterminating Users")
    users.foreach { user =>
      try {

        val username = user.getUserName()

        val userKeys = {
          val req = new ListAccessKeysRequest()
          req.setUserName(username)
          val aks = iam.listAccessKeys(req)
          aks.getAccessKeyMetadata.asScala filter { (k => k.getAccessKeyId() != key) }
        }

        userKeys foreach {
          k =>
            {
              info(this, s"Exterminating access key [$k.getAccessKeyId()] from user [$username]")
              Humid {
                iam.deleteAccessKey(new DeleteAccessKeyRequest().withUserName(username).withAccessKeyId(k.getAccessKeyId()))
              }
            }
        }

        val gs = iam.listGroupsForUser(new ListGroupsForUserRequest().withUserName(username)).getGroups().asScala
        gs foreach { g =>
          {
            info(this, s"Exterminating membership of user [$username] in group [${g.getGroupName()}]")
            Humid {
              iam.removeUserFromGroup(new RemoveUserFromGroupRequest().withGroupName(g.getGroupName()).withUserName(username))
            }
          }
        }

        val ps = iam.listUserPolicies(new ListUserPoliciesRequest().withUserName(username)).getPolicyNames().asScala
        ps.foreach { p =>
          info(this, s"Exterminating policy [$p] of user [$username]")
          Humid {
            iam.deleteUserPolicy(new DeleteUserPolicyRequest().withUserName(username).withPolicyName(p))
          }
        }

        val olp = Option(iam.getLoginProfile(new GetLoginProfileRequest().withUserName(username)).getLoginProfile())
        olp foreach { lp =>
          info(this, s"Exterminating login profile created at [${lp.getCreateDate}] of user [$username]")
          Humid {
            iam.deleteLoginProfile(new DeleteLoginProfileRequest().withUserName(username))
          }
        }

        Humid {
          info(this, s"Deleting user $username")
          iam.deleteUser(new DeleteUserRequest().withUserName(username))
        }
      } catch {
        case e: Exception => info(this, "Failed to exterminate user [" + user.getUserName() + "]: " + e.getMessage())
      }
    }

    info(this, "Exterminating Groups")
    groups.foreach { group =>
      iam.listGroupPolicies(new ListGroupPoliciesRequest().withGroupName(group.getGroupName())).getPolicyNames().asScala foreach {
        policy =>
          {
            info(this, s"Exterminating policy [$policy] in group [${group.getGroupName()}]")
            Humid {
              iam.deleteGroupPolicy(new DeleteGroupPolicyRequest().withGroupName(group.getGroupName()).withPolicyName(policy))
            }
          }
      }
      info(this, s"Exterminating group [${group.getGroupName()}]")
      Humid {
        iam.deleteGroup(new DeleteGroupRequest().withGroupName(group.getGroupName()))
      }
    }

    info(this, "Exterminating Roles")
    roles.foreach { role =>
      //TODO: Exterminate roles with policies and instances

      val roleName = role.getRoleName()

      info(this, s"Exterminating Instance Profiles for Role [$roleName]")
      val ipsfr = iam.listInstanceProfilesForRole(
        new ListInstanceProfilesForRoleRequest()
          .withRoleName(roleName))
        .getInstanceProfiles().asScala
      ipsfr foreach { ip =>
        info(this, s"Removing instance profile [${ip.getInstanceProfileName()}] from role [$roleName]")
        Humid {
          iam.removeRoleFromInstanceProfile(
            new RemoveRoleFromInstanceProfileRequest()
              .withInstanceProfileName(ip.getInstanceProfileName)
              .withRoleName(roleName))
        }
      }

      val policies = iam.listRolePolicies(new ListRolePoliciesRequest().withRoleName(roleName)).getPolicyNames().asScala
      policies foreach { policy =>
        info(this, s"Deleting role policy [$policy] from role [$roleName]")
        Humid {
          iam.deleteRolePolicy(new DeleteRolePolicyRequest().withPolicyName(policy).withRoleName(roleName))
        }
      }

      info(this, s"Exterminating role [${role.getRoleName()}]")
      Humid {
        iam.deleteRole(new DeleteRoleRequest().withRoleName(roleName))
      }
    }

    info(this, "Exterminating instance profiles")
    val ips = iam.listInstanceProfiles().getInstanceProfiles().asScala
    ips foreach { ip =>
      info(this, s"Exterminating Instance Profile [${ip.getInstanceProfileName()}]")
      Humid {
        iam.deleteInstanceProfile(new DeleteInstanceProfileRequest().withInstanceProfileName(ip.getInstanceProfileName()))
      }
    }

  }
}
