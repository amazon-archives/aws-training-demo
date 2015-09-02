/*
Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License.A copy of the License is located at

    http://aws.amazon.com/apache2.0/

or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
*/

package com.amazon.aws.training.controler;

import com.amazon.aws.training.backend.GreeterEJBBean;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import java.io.Serializable;

/**
 * A simple managed bean that is used to invoke the GreeterEJB and store the
 * response. The response is obtained by invoking getMessage().
 *
 */
@Named("greeter")
@SessionScoped
public class Greeter implements Serializable {

    @EJB
    private GreeterEJBBean greeterEJB;

    private String message;

    public void setName(String name) {
        if (name.equalsIgnoreCase(""))
            message = "";
        else
            message = greeterEJB.sayHello(name);
    }

    public String getMessage() {
        return message;
    }

}
