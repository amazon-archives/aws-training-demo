// 
// Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
//
//     http://aws.amazon.com/apache2.0/
//
// or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

var lambda  = require('./cloudtrail.js')
var event   = require('./input.json')

var context = {}
context.done = function(arg1, arg2) {
  console.log('context.done')
}

//TODO should invoke this code under an AssumeRole context
//to mimic Lambda's behaviour

lambda.handler(event, context)
