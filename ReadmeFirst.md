# Client and Server architecture #

MiniG is splitted in 2 applications :
  * A backend web service built using eclipse server technologies
  * A frontend based on Google Web Toolkit that communicates with the backend web service through HTTP.

The backend handles IMAP & SMTP connections, builds caches of user mailboxes to group messages as conversations and does full text indexing of emails

The frontend only connects to the backend and uses the caches built on the backend side to provide a responsive AJAX user interface.

# Setup from binaries #

## Frontend ##

  * Download the minig frontend war file from the [downloads page](http://code.google.com/p/minig/downloads/list).
  * Put it in the webapps directory of a Tomcat 6.0.x server
  * start tomcat
  * go to http://localhost:8080/minig-frontend-0.xx where xx is the version number in the downloaded file name.

Without any further configuration, the frontend runs on dummy data generated on the fly for testing purposes.

# Setup from SVN #

Getting MiniG up and running from an svn checkout is describe in the SetupFromSources page.