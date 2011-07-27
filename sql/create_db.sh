#!/bin/bash

test $# -eq 3 || {
    echo "usage: $0 db user host"
    exit 1
}

psql=$(which psql)
test "$psql" != "" || {
    echo "psql: not found, please install postgresql-client package"
    exit 1
}
db=$1
user=$2
host=$3


$psql -U ${user} ${db} -h ${host} -f \
create_minig.sql > /tmp/minig.log 2>&1
grep -i error /tmp/minig.log && {
    echo "error in pg script, look at /tmp/minig.log"
    exit 1
}

echo "DONE."
