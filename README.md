# Kdash-Upload-Servlet

This is a simple Java servlet, to be deployed with Tomcat.

This servlet receives KITE tests' allure report files in zip format. 
At the end of a test, the KITE engine compress the 'kite-allure-reports' in to a zip file and send to a callback url,
configurable in the test's config file. The url should point to this servlet.

## How this works

When allure report zip files are sent to the servlet, it will do the followings:
 - Unzip the file to a temp folder
 - Generate the allure web files to a specific folder,
  ready to be served (`/var/www/allure` on Linux/Ubuntu and `C:\nginx\html\allure` on windows)
 - It will overwrite the current results if any.
 

## Prerequisites

For this project, these followings are needed:
 - Java (current default-jdk package is 11)
 - Maven (3.6)
 - Tomcat (9), the instructions to install is [here](https://linuxize.com/post/how-to-install-tomcat-9-on-ubuntu-18-04/)
 - Allure-commandline (2.13)


## How to setup stand-alone

Clone this repository

`git clone https://github.com/CoSMoSoftware/Kdash-Upload-Servlet.git`

Compile the project:

`mvn clean install`

After this, you should have a file call `kdash.war` in the `target` folder.
Copy this file to `PATH_TO_TOMCAT/webapps`

Start tomcat, the servlet will be ready to receive at `http://localhost:8080/kdash/upload`

After receiving the result, it will generate the Allure web files in the designated folders 
mentioned above.


## How to setup with web server
This servlet is supposed to work with a configured dashboard VM on AWS, using nginx to serve the Allure report.

We have an example AMI on aws with every thing installed here : ami-00ee040328680d0a3. 
You can look for this in the AWS Community AMIs catalog. However this is only available
in the region us-east-1.

We recommend you setting up your own, since ours might have stuff that you don't need

### Use Cosmo AMI:

The AMI has been set up to use SSL. So you will need a domain name to generate the certificate with certbot.
Take a look [here](https://medium.com/@saurabh6790/generate-wildcard-ssl-certificate-using-lets-encrypt-certbot-273e432794d7)

When you have your certificates, modify `/etc/openresty/nginx.conf` with them:

```
server {   
 listen       443 ssl;                                                                                                                                    
 #charset koi8-r;
 #access_log  logs/host.access.log  main;
 root /var/www/allure;
 # Add index.php to the list if you are using PHP
 index index.html index.htm index.nginx-debian.html;                                                                                                      
 server_name dashboard.cosmosoftware.io;
 ssl_certificate      /etc/letsencrypt/live/dashboard.cosmosoftware.io/fullchain.pem;
 ssl_certificate_key  /etc/letsencrypt/live/dashboard.cosmosoftware.io/privkey.pem;
 location ~* .(?:ico|css|js|json|xml|svg|gif|jpe?g|png|svg|woff)$ {
                expires 15m;
                add_header Pragma public;    
                add_header Cache-Control "public";
                add_header ETag "";
                }
...
```

All you need now is to restart openresty `sudo systemctl restart openresty` for the certificates 
to take effect.

Start tomcat with `sudo systemctl start tomcat`


### Setup your own VM 
You can setup your own nginx to serve the above folders.

- Install nginx :
```
sudo apt-get update
sudo apt-get install nginx
```

Create a directory in /var/www/ called allure. This is where your report files will go.

- Configure NGINX to serve your files:

`cd` into `/etc/nginx/`. This is where the NGINX configuration files are located.

The two directories we are interested are `sites-available` and `sites-enabled`.
      
      - `sites-available` contains individual configuration files for all of your possible static websites.
      
      - `sites-enabled` contains links to the configuration files that NGINX will actually read and run.

What we’re going to do is create a configuration file in sites-available, and then create a symbolic link (a pointer) to that file in sites-enabled to actually tell NGINX to run it.

Create a file called `YOUR_DOMAIN_NAME` in the sites-available directory and add the following text to it:

```
server {
  listen 80 default_server;
  listen [::]:80 default_server;  
  root /var/www/allure;  
  
  index index.html;  
  
  server_name your-domain.com www.your-domain.com;  
  location / {
    try_files $uri $uri/ =404;
  }
}
```

Now that the file is created, we’ll add it to the sites-enabled folder to tell NGINX to enable it. The syntax is as follows:

```ln -s <SOURCE_FILE> <DESTINATION_FILE>```

The actual syntax will look like:

```ln -s /etc/nginx/sites-available/YOUR_DOMAIN /etc/nginx/sites-enabled/YOUR_DOMAIN```

Now, if you were to restart NGINX you should see your site!

```sudo systemctl restart nginx```

After this, your nginx should be ready to serve whatever in `/var/www/allure`

All you need now is to setup this servlet on your VM.
Make sure to install all the prerequisites and it should be OK!
