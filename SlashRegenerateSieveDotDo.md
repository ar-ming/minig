# Description #

Regenerate a user sieve script using minig\_filters table content.

# URL #

POST method on /regenerateSieve.so

# Parameters #

| Parameter | Description |
|:----------|:------------|
| login     | The login (at domain) of the user |
| password  | The user password |

# Response #

The methods returns an UTF-8 text/plain response with the folowing content :

```
OK
```

When the method fails :

```
KO - look in /var/log/minig/minig-backend.log for detailed error report.
```

# Remarks #

  * This method does not trigger mailbox indexing.
  * This API should be improved to allow authentification as cyrus admin.
  * This method does not take vacation settings into account. But it will before 2.3.0-rc release of OBM.