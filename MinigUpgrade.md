# Introduction #

This document explains how to do a minig upgrade.


# Procedure #

Step 0 : cut out your users from minig

Step 1 : upgrade your debian packages

Step 2 : get full\_reset.sh & minig\_init\_index.py from SVN

Step 3 : Reset your minig indexes

```
./full_reset.sh
```

will reset minig cache & solr index (if solr runs from the same machine as minig backend).

Step 4 : pre-init user minig caches :

```
./minig_init_index.py localhost 8081
```

wait until your /var/log/obm-tomcat/obm-java.log stops writing lines about things added to solr

Step 5 :
Once your solr seems ok, restart everything :

```
/etc/init.d/minig-backend restart
/etc/init.d/obm-tomcat restart
```

Step 6 :
re-open access to your users