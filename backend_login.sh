#!/bin/bash

test $# -eq 3 || {
    echo "usage: $0 login domain password"
    exit 1
}

function post_data() {
    http_r=`echo "$1" | \
	lynx -post_data -dump http://localhost:8081/$2`
}

post_data "login=$1&domain=$2&password=$3" firstIndexing.do

token=`echo ${http_r} | sed -e 's/.*value="\([^"]*\).*/\1/'`

echo -e "logged in with token: ${token}"

#sleep 15
#post_data "token=${token}" logout.do
