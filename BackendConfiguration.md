# Introduction #

MiniG backend service is configured with 4 files :
  * /etc/minig/backend\_conf.ini
  * /etc/minig/account\_conf.ini
  * /etc/minig/indexing\_conf.ini
  * /etc/minig/obmsync\_conf.ini

# Configuration files #

## /etc/minig/backend\_conf.ini ##

```
# IMAP & SMTP server configuration
backend.imap.uri=imap://obm.buffy.kvm
backend.imap.singleDomain=false
backend.sieve.uri=sieve://obm.buffy.kvm
backend.smtp.uri=smtp://obm.buffy.kvm

#
# ldap address book. %q is replaced by the query.
#
completion.ldap.filter=(&(objectClass=inetOrgPerson)(mail=%q*))
completion.ldap.url=ldap://obm.buffy.kvm
completion.ldap.basedn=dc=local
```

## /etc/minig/account\_conf.ini ##

This files indicates the default folders used for trash, sent & drafts folders. They are automatically created & subscribed if they don't exist.

```
# MiniG Default account setup
# %d is replaced by IMAP delimiter

# a webmail_cache directory will be created here
account.caches.folder=/var/cache/minig

# minig pg database setup
account.caches.jdbc.driver=org.postgresql.Driver
account.caches.jdbc.url=jdbc:postgresql://127.0.0.1/minig
account.caches.jdbc.login=minig
account.caches.jdbc.password=minig


# those settings are ok when altnamespace:yes is configured on Cyrus IMAP
account.folders.sent=Sent
account.folders.trash=Trash
account.folders.drafts=Drafts
account.folders.templates=Templates
account.folders.spam=SPAM

# those folders are skipped by full text indexing. Not case sensitive
account.folders.skipped=spam,junk
```

## /etc/minig/indexing\_conf.ini ##

This files indicates where a solr indexing server is available for conversations indexing.

```
solr.server.url=http://obm.buffy.kvm:8080/solr/webmail
```

For more details on full text indexing setup have a look at FullTextIndexing

## /etc/minig/obmsync\_conf.ini ##

This file indicate which obm-sync server should be used to validate event invitations received by email.

```
obmsync.server.url=https://obm/obm-sync/services
```

# Network setup #

When deployed in an OBM environnement, minig backend connects to various network services :
  * OBM database (located using /etc/obm/obm\_conf.ini)
  * OBM sync server (located using /etc/minig/obmsync\_conf.ini)
  * An LDAP server for address book (only anonymous connections right now)
  * IMAP server (port 143) and timsieved (port 2000)
  * SMTP server