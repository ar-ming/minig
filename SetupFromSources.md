# Setup from source #

## Checkout ##

svn co http://minig.googlecode.com/svn/trunk/ minig

## Repository structure ##

the plugins & scripts directory holds the backend code.

webmail-frontend holds the various frontends

webmail-frontend/gwt holds a Google Web Toolkit based AJAX frontend
capable of using the backend.

Build / Run scripts are provided.

## Building the backend ##

### Import in eclipse ###

File > Import... > General / Existing projects into workspace > Next

"Select root directory" and browse to your "minig/plugins" folder

Select all the fr.aliasource.webmail projects

DO NOT check "copy projects into workspace"

Set Compiler compliance level to "6.0" in Eclipse (code relies on annotations).

Finish

### Creating an executable backend ###

MiniG backend is based on equinox server platform : you need an eclipse >= 3.5.x (Galileo) installed with GWT plugin (install doc is here : http://code.google.com/intl/fr/eclipse/docs/getting_started.html)

To build the backend, use the product\_build.sh script. This scripts
invokes an headless eclipse PDE build (i.e. you need eclipse installed somewhere to build it).

Copy product\_build.properties to <your
login>.product\_build.properties.

Point <your login>.product\_build.properties to your eclipse
installation :
  * the pdev variable is the version of the org.eclipse.pde.build plugin in your installation
  * the equiv variable is the version of the org.eclipse.equinox.launcher plugin in your installation

Run ./product\_build.sh

The output should look like this (when run as user 'tom') :

```
 Loading ./tom.product_build.properties
 Found valid PDE & Equinox launcher.
 Setting up build dir & build conf...Done.
 Invoking PDE build...
 Build finished.
 PDE built package is 'minig-backend-svn-linux.gtk.x86.zip'
 Adding launch scripts to archive...Done.
 Product is in 'minig-backend-svn-linux.tar.bz2'
 Build finished.
```

At the end of the build, minig-backend-svn-linux.tar.bz2 contains a
runnable backend. The backend embeds equinox http server (Jetty), so
no further setup is needed.

NOTE for debian packaging : PDE build breaks in very strange
ways when running under fakeroot. To package it, use a previously
built backend.

### Running ###

Copy/edit backend configuration as decribed [here](BackendConfiguration.md)

After successfull build, the test\_product\_build.sh script will unpack the
backend & start it.

The output should be :

```
2008-03-12 08:12:56,351 Application INFO - MiniG backend starting...
2008-03-12 08:12:56,539 HttpServer INFO - Version Jetty/5.1.x
2008-03-12 08:12:56,630 Container INFO - Started org.mortbay.jetty.servlet.ServletHandler@1712651
2008-03-12 08:12:56,630 Container INFO - Started HttpContext[/,/]
2008-03-12 08:12:56,636 SocketListener INFO - Started SocketListener on 0.0.0.0:8081
2008-03-12 08:12:56,636 Container INFO - Started org.mortbay.http.HttpServer@383118
2008-03-12 08:12:56,683 Controller INFO - Controller created.
2008-03-12 08:12:56,809 RunnableExtensionLoader INFO - BookSource loaded.
2008-03-12 08:12:56,817 Configuration INFO - fr.aliasource.webmail.ldap initialised, url: ldap://obm.buffy.kvm basedn: dc=local filter: (&(objectClass=inetOrgPerson)(mail=%q*)) (valid conf: true)
2008-03-12 08:12:56,817 BookSource INFO - ldap book source initialised.
2008-03-12 08:12:56,818 AllContacts INFO - AllContacts virtual source created  for 1 sources
2008-03-12 08:12:56,819 BookManager INFO - AddressBook Manager created with 2 source(s)
2008-03-12 08:12:56,819 RunnableExtensionLoader INFO - CompletionSourceFactory loaded.
2008-03-12 08:12:56,820 CompletionRegistry INFO - 1 completion factories registered.
2008-03-12 08:12:56,859 RunnableExtensionLoader INFO - PlainBodyFormatter loaded.
2008-03-12 08:12:56,860 RunnableExtensionLoader INFO - HTMLBodyFormatter loaded.
2008-03-12 08:12:56,874 IndexingActivator INFO - Indexing plugin activated
2008-03-12 08:12:56,875 RunnableExtensionLoader INFO - ConversationListenerFactory loaded.
2008-03-12 08:12:56,880 RunnableExtensionLoader INFO - ContactGroupsAction loaded.
2008-03-12 08:12:56,881 RunnableExtensionLoader INFO - GetContactsAction loaded.
2008-03-12 08:12:56,882 RunnableExtensionLoader INFO - SearchAction loaded.
2008-03-12 08:12:56,895 RunnableExtensionLoader INFO - SendAction loaded.
2008-03-12 08:12:56,895 ActionRegistry INFO - registered action '/contactGroups.do' from plugin.
2008-03-12 08:12:56,895 ActionRegistry INFO - registered action '/contacts.do' from plugin.
2008-03-12 08:12:56,895 ActionRegistry INFO - registered action '/search.do' from plugin.
2008-03-12 08:12:56,895 ActionRegistry INFO - registered action '/sendMessage.do' from plugin.
2008-03-12 08:12:56,895 ActionRegistry INFO - Registered 4 plugins provided actions in controller.
```

### Tips and Tricks ###

With recent debian distributions, java processes are only able to bind to ipv6 addresses.
It's probably not what you expect your system to do.
If your minig-backend can't establish a link to your obm environment, check your `/etc/sysctl.d/bindv6only.conf` file.

## GWT Frontend ##

### Step 1 : get external dependencies ###

Checkout and build GWT from SVN (upcoming 2.0 release needed) from http://code.google.com/intl/fr/webtoolkit/makinggwtbetter.html#workingoncode

The directory where GWT unpacked is GWT\_HOME.

### Step 2 : create user specific setup ###

`./gen_run_scripts.sh <GWT_HOME>`

Use an absolute path when running this script, otherwise the WebmailUI.launch launch configuration will not work.

### Step 3 : build as a webapp ###

`ant dist`

The build.xml file is configured to create a complete webapp structure
in the 'dist' directory.

### Step 4 : configure frontend ###

Copy/edit backend configuration as decribed [here](FrontendConfiguration.md)

Once step 1, 2, 3 and 4 are completed :

### Running in GWT shell ###

`./run_in_shell.sh`

(just click on "login")

### Running in Tomcat ###

Put tomcat 6.0.18 archive in the current dir

`./run_in_tomcat.sh`

Point your browser to http://localhost:8080/

The paragraph "Production setup" (in this file) has more informations
on tomcat based installation.

Note : the default configuration runs without IMAP server & minig-backend


### Import in eclipse ###

WARNING : step 1 & 2 are mandatory to get a correct eclipse project
setup.

File > Import... > General / Existing projects into workspace > Next

"Select root directory" and browse to your "webmail-fronted/gwt" folder

Select the WebmailUI project

DO NOT check "copy projects into workspace"

Set Compiler compliance level to "6.0" in Eclipse (code relies on annotations).

To run in eclipse, click the "Run" button in the toolbar. This uses
the WebmailUI.lauch configuration.

### Production setup ###

Default configuration runs without webmail backend & IMAP server,
using a dummy backend client implementation.

For production/real setup, copy frontend\_conf.ini to the
/etc/minig directory and :

  * Set the frontend.gwt.proxyClientFactoryClass variable to the real backend client factory implementation.

  * Adjust the frontend.proxyUrl variable to point your webmail backend. Valid values are :
    1. fr.aliasource.webmail.server.proxy.client.DummyProxyClientFactory
    1. fr.aliasource.webmail.server.proxy.client.http.ProxyClientFactory

  * Deploy the webapp built with "ant dist" to your tomcat server (using the run\_in\_tomcat.sh script works too for testing a complete setup).

  * Point your browser to http://localhost:8080/