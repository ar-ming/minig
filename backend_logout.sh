#!/bin/bash

test $# -eq 1 || {
    echo "usage: $0 token"
    exit 1
}

token=$1

function post_data() {
    http_r=`echo "$1" | \
	lynx -post_data -dump http://localhost:8081/$2`
}

post_data "token=${token}" logout.do
