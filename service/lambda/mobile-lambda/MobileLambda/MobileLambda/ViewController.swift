// Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License").
// You may not use this file except in compliance with the License.
// A copy of the License is located at
//
//     http://aws.amazon.com/apache2.0/
//
// or in the "license" file accompanying this file.
// This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for
// the specific language governing permissions and limitations under the License.

import UIKit

// TODO in AWS Console or CLI
// 1. Create a Cognito Identity Pool and give the pool ID to AWSCognitoCredentialsProvider below
// 2. Associate a unauthenticate role authorised to call your lambda function

class ViewController: UIViewController {

    @IBOutlet weak var name: UITextField!
    @IBOutlet weak var send: UIButton!
    @IBOutlet weak var greeting: UILabel!
    @IBOutlet weak var deviceType: UILabel!

    override func viewDidLoad() {
        super.viewDidLoad()

        self.send.enabled = false

        //initialize Cognito ID
        NSLog("Creating AWS Cognito Credentials Provider")
        let credentialsProvider = AWSCognitoCredentialsProvider(regionType:  AWSRegionType.EUWest1, identityPoolId: "eu-west-1:identity_pool_id")

        NSLog("Creating a default AWSservice configuration for unauthenticated user")
        let configuration = AWSServiceConfiguration(region: AWSRegionType.EUWest1, credentialsProvider: credentialsProvider)
        AWSServiceManager.defaultServiceManager().defaultServiceConfiguration = configuration

        //no need to authenticate, for this app, we'e going with unauthenticated users

    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    @IBAction func editingChanged(sender: UITextField) {
        if (self.name.text!.characters.count > 0) {
            self.send.enabled = true
        } else {
            self.send.enabled = false
        }
    }

    @IBAction func sendAction(sender: UIButton) {

        dispatch_async(dispatch_get_main_queue(), {
            SVProgressHUD.showWithStatus("Calling Lambda")
            self.name.resignFirstResponder()
        })

        //collect name
        let firstname = self.name.text!

        //invoke lambda function asynchronously
        NSLog("Invoking lambda function for firtsname=\(firstname)")
        let lambdaInvoker = AWSLambdaInvoker.defaultLambdaInvoker()
        let task = lambdaInvoker.invokeFunction("mobile-lambda", JSONObject: ["firstName":firstname])

        task.continueWithBlock({ (task: AWSTask!) -> AWSTask! in

            if (task.error != nil) {
                NSLog("Invoke Lambda returned an error : \(task.error)")
                dispatch_async(dispatch_get_main_queue(), {
                    self.greeting.text = "Error"
                    self.deviceType.text = task.error.description
                    SVProgressHUD.dismiss()
                })
            } else {
                if (task.result != nil) {
                    NSLog("Invoke Lambda : result = \(task.result)")

                        //upate text label on the main UI thread
                        dispatch_async(dispatch_get_main_queue(), {
                            let r = task.result as! Dictionary<String,String>
                            self.greeting.text = r["message"]
                            self.deviceType.text = r["device"]
                            SVProgressHUD.dismiss()
                        })

                } else {
                    NSLog("Invoke Lambda : unknow result : \(task)");
                    NSLog("Exception : \(task.exception)")
                    NSLog("Error : \(task.error)" )
                    dispatch_async(dispatch_get_main_queue(), {
                        self.greeting.text = "Error"
                        SVProgressHUD.dismiss()
                    })
                }
            }
            return nil
        })
    }
}
