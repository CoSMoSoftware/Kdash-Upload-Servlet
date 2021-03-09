# Kdash Simplified version

This is a simple Java servlet, to be deployed with Tomcat.

This servlet lists KITE tests' allure reports in /var/www/allure. 

## Prerequisites

For this project, these followings are needed:
 - Java (current default-jdk package is 11)
 - Maven (3.6)
 - Tomcat (9), the instructions to install is [here](https://linuxize.com/post/how-to-install-tomcat-9-on-ubuntu-18-04/)

## How to setup stand-alone

Clone this repository

`git clone https://github.com/CoSMoSoftware/Kdash-Simplified.git`

Compile the project:

`mvn clean install`

After this, you should have a file call `kdash.war` in the `target` folder.
Copy this file to `PATH_TO_TOMCAT/webapps`

The Kdash is accessible through http://localhost:8080/kdash
