# Introduction #

The Google Web Toolkit front-end for MiniG is configured with the following file(s) :
  * /etc/minig/frontend\_conf.ini

# /etc/minig/frontend\_conf.ini #

```
########################################################
# General Frontend settings                            #
# copy this file to /etc/minig/frontend_conf.ini       #
########################################################

# IMAP proxy location (MiniG backend location)
frontend.proxyUrl=http://localhost:8081

# Set to "true" if frontend handles login by itself . If "false", a
# previous app layer MUST perform login before showing the frontend.
#
# With the GWT frontend running in tomcat, a servlet filter might perform
# the login operation.
frontend.ajaxLogin=true

# those settings are needed when OBM acts as single sign on provider for MiniG
frontend.ssoProvider=fr.aliasource.webmail.server.OBMSSOProvider
frontend.ssoServerUrl=https://obm.buffy.kvm/sso/sso_index.php
frontend.logoutUrl=https://obm.buffy.kvm/obm.php?action=logout

####################################
# gwt front specific settings      #
####################################

# client implementation

# uncomment for the dummy implementation 
#frontend.gwt.proxyClientFactoryClass=fr.aliasource.webmail.server.proxy.client.DummyProxyClientFactory

# real client implementation
frontend.gwt.proxyClientFactoryClass=fr.aliasource.webmail.server.proxy.client.http.ProxyClientFactory
```

This configuration needs the OBM interface, which will act as an SSO server for MiniG. To deploy without OBM, you must edit the frontend web.xml file and disable the filter & filter-mapping defined there.