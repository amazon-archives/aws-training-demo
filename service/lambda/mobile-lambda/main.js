// Copyright 2015, Amazon Web Services.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

var lambda  = require('./mobile-lambda.js')
var event   = require('./input.json')

var context = {
   "clientContext" : {
       "env" : {
         "make" : "home made",
         "model" : "my phone"

      }
   }
}
//var context = {}
context.done = function(error, result) {
  console.log('context.done');
  console.log(error);
  console.log(result);
}
context.succeed = function(result) {
  console.log('context.succeed');
  console.log(result);
}
context.fail = function(error) {
  console.log('context.fail');
  console.log(error);
}

//TODO should invoke this code under an AssumeRole context
//to mimic Lambda's behaviour

lambda.handler(event, context)
