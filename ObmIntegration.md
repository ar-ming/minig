# Introduction #

Specifications of the features we provide by using data or services from OBM.
Most of those features are integrated through plugins, so that OBM can stay an optional dependency for Minig standalone installs.

# Settings integration #

org.minig.settings.obm will push settings from obm database to minig settings infrastructure.

In current svn, minig can access obm user preferences. MiniG does not use all of them right now, but language is used for i18n selection.

# Writable addressbook and email address gathering #

Push email used in to/cc/bcc of sent mail into obm private contacts.

This is implemented in current svn & requires a recent obm 2.2 database. User private contacts & public contacts are also available for email completion.

# Event email notifications, ICS attachments and ITIP #

MiniG does a special processing of event notifications sent by OBM or Outlook. It processes the attached ICS file & allow insertion in the OBM calendar without leaving the webmail.