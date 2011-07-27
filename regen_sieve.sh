#!/bin/bash

test $# -eq 2 || {
    echo "usage: $0 login@domain password"
    exit 1
}

function post_data() {
    http_r=`echo "$1" | \
	lynx -post_data -dump http://localhost:8081/$2`
}

post_data "login=$1&password=$2" regenerateSieve.do

echo ${http_r}
