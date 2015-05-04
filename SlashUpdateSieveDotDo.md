# Description #

Writes a sieve script on the server.

# URL #

POST method on /updateSieve.so

# Parameters #

| Parameter | Description |
|:----------|:------------|
| login     | The login (at domain) of the user |
| password  | The user password |
| scriptName  | The name of the script that should be overriden |
| content  | The sieve script content that will be store in "scriptName" script |

# Response #

The methods does not send a response.

# Remarks #

  * This method does not trigger mailbox indexing.
  * This API should be improved to allow authentification as cyrus admin.