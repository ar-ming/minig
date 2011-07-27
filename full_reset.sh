#!/bin/bash
# This script does a complete reset of minig backend and solr indexes

/etc/init.d/minig-backend stop
/etc/init.d/obm-tomcat stop
rm -fr /var/lib/minig-backend/org.*
rm -fr /var/cache/minig/webmail_cache/*
rm -fr /var/solr/webmail/data/index
/etc/init.d/obm-tomcat start
/etc/init.d/minig-backend start
