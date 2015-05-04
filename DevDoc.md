# Introduction #

This document should introduce MiniG architecture to wannabe developers.

# 2 applications #

MiniG is divided in two Java applications :
  * A "classic" java web application, running on tomcat : the frontend.
  * A java daemon, based on  [OSGi / Eclipse plugin technologies](http://www.eclipse.org/equinox/server/).

Frontend & backend communicates using a REST style http protocol.

# dev environnement setup #

You will need eclipse 3.5 or 3.6. You must add the gwt eclipse plugin. Update site url is :
  * http://dl.google.com/eclipse/plugin/3.5
  * http://dl.google.com/eclipse/plugin/3.6 (for eclipse 3.6)

SetupFromSources gives further details for importing the code in eclipse or making a binary build.

# Frontend #

MiniG user interface is built using [GWT](http://code.google.com/webtoolkit/). The frontend is only connected to the backend.

## UI Entry Point ##

As every GWT ui, MiniG has an entry point : [WebmailUI](http://code.google.com/p/minig/source/browse/trunk/webmail-frontend/gwt/src/fr/aliasource/webmail/client/WebmailUI.java)

## Backend communication ##

The backend client is build using [commons-http](http://hc.apache.org/httpclient-3.x/). The [ProxyClient](http://code.google.com/p/minig/source/browse/trunk/webmail-frontend/gwt/src/fr/aliasource/webmail/server/proxy/client/http/ProxyClient.java) class provides a method for each of the backend entry points.

## SSO ##

The frontend uses a single-sign-on filter to "inherit" OBM authentification. The [SSO filter](http://code.google.com/p/minig/source/browse/trunk/webmail-frontend/gwt/src/fr/aliasource/webmail/server/LoginFilter.java) loads implementations of SSO providers to supports alternate web sso implementations.

# Backend #

MiniG backend is often called "IMAP proxy". For each logged-in users, it maintains 2 IMAP connections and creates caches & indexes of the mailboxes.

The backend connects to several subsystems :
  * LDAP directory for contacts & identity
  * OBM-Sync for contacts search, contacts gathering, ICS invitations management
  * Solr for email indexing
  * IMAP server for fetching emails
  * SMTP server for sending
  * timsieved for writing filters

Most of those connections are implemented as plugins and should be easily replacable (except for the IMAP part...)

## HTTP API ##

The backend http API is documented on [Backend API](BackendAPI.md)