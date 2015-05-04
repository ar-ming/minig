# Introduction #

MiniG debian packages are available on obm.org

Up to date informations on installation are maintained on [obm.org](http://www.obm.org/doku.php?id=docs:install:debian).

# Details #

add to your /etc/apt/sources.list :

```
deb http://www.backports.org/debian etch-backports main contrib non-free
#deb http://www.backports.org/debian lenny-backports main contrib non-free
deb http://deb.obm.org/22 obm obm
```

Then do :

```
wget -q  http://deb.obm.org/obmgpg.pub -O - | apt-key  add -
aptitude update
aptitude install obm
aptitude install obm-solr minig-conf minig-backend minig minig-storage
```

# Remarks #

obm installation is needed to get the correct openldap, cyrus and postfix servers setup. Those packages are intended to run on dedicated obm+minig servers. They will (re)write your cyrus, postfix & slapd configuration.