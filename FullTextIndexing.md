# Introduction #

MiniG uses [solr](http://lucene.apache.org/solr/) for full text indexing


# Details #

To configure solr for MiniG, you need the [schema.xml](http://minig.googlecode.com/files/schema.xml) file available in MiniG download section.

# Backend configuration #

The backend needs the _indexing\_conf.ini_ configuration file with a content like in the example below :

```
solr.server.url=http://10.0.0.5:8080/solr/webmail
```

# Solr servlet configuration #

solr can be deployed on a separate tomcat server (recommanded) or in the frontend tomcat (for fast installation).

We use a context file similar to the one below :

```
<?xml version="1.0" encoding="utf-8"?>
<Context path="/solr" docBase="/usr/share/solr">
    <Environment name="/solr/home" type="java.lang.String" value="/var/solr"/>
</Context>
```

To get this setup working :
  * expand http://minig.googlecode.com/files/obm-solr.tar.bz2 in /usr/share
  * mkdir -p /var/solr/webmail/data /var/solr/webmail/conf

Add a solr.xml file in /var/solr :

```
<?xml version="1.0" encoding="UTF-8" ?>
<solr persistent="false">
  <cores adminPath="null" >
    <core name="webmail" instanceDir="webmail"/>
  </cores>
</solr>
```

Add solrconfig.xml in /var/solr/conf/webmail :

```
<?xml version="1.0" encoding="UTF-8" ?>

<config>
  <updateHandler class="solr.DirectUpdateHandler2" />

  <requestDispatcher handleSelect="true" >
    <requestParsers enableRemoteStreaming="false" multipartUploadLimitInKB="2048" />
  </requestDispatcher>
  
  <requestHandler name="standard" class="solr.StandardRequestHandler" default="true" />
  <requestHandler name="/update" class="solr.XmlUpdateRequestHandler" />
  <requestHandler name="/admin/luke"       class="org.apache.solr.handler.admin.LukeRequestHandler" />
  
  <!-- config for the admin interface --> 
  <admin>
    <defaultQuery>solr</defaultQuery>
    <gettableFiles>solrconfig.xml schema.xml admin-extra.html</gettableFiles>
    <pingQuery>
      qt=standard&amp;q=solrpingquery
    </pingQuery>
  </admin>

</config>
```

Add the schema.xml from http://minig.googlecode.com/files/schema.xml in /var/solr/webmail/conf