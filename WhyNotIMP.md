# Introduction #

MiniG is a work in progress. Right now, it is designed for an OBM messaging system and can work if you have an openldap user directory and a cyrus imap server. It is not tested on anything else, but might work with any imap server with uidplus extension support.


# How MiniG differs from IMP #

MiniG has a few differences with IMP :
  * it is written in java, based on an osgi server side architecture and gwt for its frontend.
  * MiniG uses a client / server model to separate its user interface from the IMAP connected part
  * MiniG frontend attempts to clone gmail
  * MiniG will not have free for all settings. I strongly believe in **good defaults**. So user's tunable settings will come only if something has a good reason to differ between users.
  * IMP is production quality software while MiniG is still in heavy development.

# Why another webmail ? #

GMail was the first webmail I preferred over thunderbird or evolution. GMail and google shown me that a webmail is not something you use only because you can't connect to your company IMAP server.

MiniG architecture is designed to provide features that only non-web clients could provide. The real IMAP client for a user runs in a separate process (the minig backend) and has a different lifecycle that the web frontend :
  * background indexing on mailboxes
  * can benefit from IMAP IDLE
  * caches data for faster web access