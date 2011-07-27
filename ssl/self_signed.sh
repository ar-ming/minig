#!/bin/bash

test $# -eq 1 || {
    echo "usage: $0 <server common name>"
    exit 1
}

cat >> $1_ssl.cnf <<EOF
[ req ]
default_bits = 2048
encrypt_key = yes
distinguished_name = req_dn
x509_extensions = cert_type
prompt = no

[ req_dn ]
O=minig.org
OU=backend
CN=$1

[ cert_type ]
nsCertType = server

EOF

openssl req -new -x509 -days 365 -passout pass:password -config $1_ssl.cnf \
-out $1_cert.pem -keyout $1_key.pem

rm $1_ssl.cnf

openssl pkcs12  -password pass:password -export -in $1_cert.pem -inkey $1_key.pem -out minig.p12 -passin pass:password -name "minig" 
