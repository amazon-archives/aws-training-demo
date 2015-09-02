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


console.log('Loading Lambda function');

exports.handler = function(event, context) {
   console.log("Received event: ", event);
   console.log("Context:", context);

   //echo incoming data
   result = { "message" : "Hello " + event.firstName};

   //send mobile's model when available
   if ("clientContext" in context) {
      if ("env" in context.clientContext) {
         if ("model" in context.clientContext.env) {
            result.device = context.clientContext.env.model;
         }
      }
   }

   //context.succeed(JSON.stringify(result));        // WORKS with iOS SDK
   context.succeed(result)   // DO NOT WORK with iOS SDK
} ;
