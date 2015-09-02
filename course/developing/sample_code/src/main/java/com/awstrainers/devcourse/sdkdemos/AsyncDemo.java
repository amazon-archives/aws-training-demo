//  Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
//
//      http://aws.amazon.com/apache2.0/
//
//  or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.



package com.awstrainers.devcourse.sdkdemos;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author ciberado
 */
public class AsyncDemo {

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Future<String> lprA = executor.submit(new LongProcess("A", 1000));
        Future<String> lprB = executor.submit(new LongProcess("B", 500));
        // Uncomment the following line to see how Future.get is a blocking invocation
        //System.out.println("**** " + lprA.get());
        Future<String> lprC = executor.submit(new LongProcess("C", 800));

    }

}

class LongProcess implements Callable<String> {

    private String name;
    private int pause;

    public LongProcess(String name, int pause) {
        this.name = name;
        this.pause = pause;
    }

    @Override
    public String call() throws Exception {
        System.out.println("Into call  " + name);
        Thread.sleep(pause);
        System.out.println("Exiting call " + name);
        return "Ok from " + name;
    }

}
