# Description #

Returns the name & content of the active sieve script, or NONE if no script is active.

# URL #

POST method on /getActiveSieve.so

# Parameters #

| Parameter | Description |
|:----------|:------------|
| login     | The login (at domain) of the user |
| password  | The user password |

# Response #

The methods returns an UTF-8 text/plain response with the folowing content :

```
minig-10f1080e-c63e-4927-8cf5-585863c40743.sieve
require [ "fileinto", "imapflags", "vacation" ];
```

The first line contains the name of the active script, the other lines contains the content of the script.

When the user has no active script, the answer is :

```
NONE
```

# Remarks #

  * This method does not trigger mailbox indexing, it is suitable for testing that MiniG backend is alive in heartbeat/mon HA scripts.
  * This API should be improved to allow authentification as cyrus admin.