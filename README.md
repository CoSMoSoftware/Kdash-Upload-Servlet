# Kdash-Upload-Servlet

This is a simple Java servlet, to be deployed with Tomcat.

This servlet receives KITE tests' allure report files in zip format. 
At the end of a test, the KITE engine compress the 'kite-allure-reports' in to a zip file and send to a callback url,
configurable in the test's config file. The url should point to this servlet.

## How this works

When allure report zip files are sent to the servlet, it will do the followings:
 - Unzip the file to a temp folder
 - Generate the allure web files to a specific folder,
 to be served (`/var/www/allure` on Linux/Ubuntu and `C:\nginx\html\allure` on windows)
 - It will overwrite the current results if any.
 
This servlet is supposed to work with a pre-configured dashboard VM on AWS, using nginx to serve the Allure report.

## Prerequisite 

For this project, these followings are needed:
 - Java (current default-jdk package is 11)
 - Maven (3.6)
 - Tomcat (9)
 - Allure-commandline (2.13)


## How to setup

Clone this repository

`git clone https://github.com/CoSMoSoftware/Kdash-Upload-Servlet.git`

Compile the project:

`mvn clean install`

After this, you should have a file call `kdash.war` in the `target` folder.
Copy this file to `PATH_TO_TOMCAT/webapps`

Start tomcat, the servlet will be ready to receive at `http://localhost:8080/kdash/upload`




