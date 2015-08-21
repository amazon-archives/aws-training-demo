JavaEEDemo
==========

Simple Java EE Web application to deploy on any Java EE 7 compliant container.

This Web App consists of 

 - A stateless EJB (GreeterEJBBean.java)
 - A managed bean (Greeter.java)
 - A Java Server Page (index.xhtml)
 
Java EE technologies used :

- EJB (SLSB)
- CDI 
- JSF
- JSTL

Non Java EE Technologies used :

- Bootstrap CSS

Tested on JBoss WildFly 8.1.0

How to build ?
--------------

Using Maven, type :

```mvn3 package``` 

to compile and generate a WAR file ready to deploy

TODO
----

- Test on GlassFish 4.x
- Test on Tomee
