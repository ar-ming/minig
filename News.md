# 2009/05/26 : Status update #

Long time no news, but development is still very active :
  * complete ICS invitations support has landed
  * binary release as debian packages are done on frequent basis
  * filters are being implemented by talking to cyrus sieve daemon to get server side filtering.

# 2009/03/24 : New binary release (0.541) #

We are pleased to announce our new release with :
  * new graphical theme more integrated with OBM ui.
  * ICS rendering, has:invitation search option, invitation icon in list view
  * mail forwards rendering (which is not available in gmail nor imp ;-) )
  * calendar early preview
  * mail signature support

# 2008/11/08 : Debian packages finally released #

I am happy to annonce that an easy way to install MiniG is available for debian users. Please have a look at the DebianEtch page.

# 2008/09/30 : New binary release available #

A new release based on svn [r195](https://code.google.com/p/minig/source/detail?r=195) was made with new IMAP client, folder tree and lots of random improvements.

As the team is expanding, lots of things are worked on :
  * full html email support (david)
  * quota display (adrien)
  * folder management (christophe)
  * drafts support (david)
  * full text improvements (david)
  * conversation display speed & indexing speed work (thomas)


# 2008/09/12 : Big changes coming up #

A folder tree was integrated to replace the folder cloud, as nobody liked my innovative folder cloud.

MiniG was using ristretto for IMAP support. This lib is really cool but was too slow for operations MiniG was doing (full text indexing of a big mailbox), so a new org.minig.imap plugin is now in SVN. It uses apache mina for efficient networking and preliminary speed tests are very good.

# 2008/06/02 : News MiniG snapshots released #

This release uses GWT 1.5.0rc1. The downloadable binaries are based on svn [r101](https://code.google.com/p/minig/source/detail?r=101). This binary release is the first one with attachments upload support.

# 2008/05/02 : MiniG can send attachments #

Work on attachments support is still going on. With svn [r88](https://code.google.com/p/minig/source/detail?r=88), MiniG can now send emails with attachments. Next task is downloading attachments from messages.