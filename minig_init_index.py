#!/usr/bin/python
# -*- coding: utf-8 -*-
# 
# This script will try to create the minig indexes for all the users
# with active mail_perms in the OBM database.
#
# Depends: python-psycopg2 for postgresql or python-mysqldb for mysql.

import ConfigParser;
import os;
import sys;
import httplib;
import urllib;

# Read /etc/obm/obm_conf.ini & fetches login, domain & passwords from
# the P_UserObm table.
def fetch_user_passwords():
    print "INFO: Reading /etc/obm/obm_conf.ini..."
    config = ConfigParser.ConfigParser();
    config.readfp(open("/etc/obm/obm_conf.ini"));

    dbtype = config.get("global", "dbtype").strip();
    host = config.get("global", "host").strip();
    db = config.get("global", "db").strip();
    user = config.get("global", "user").strip();
    password = config.get("global", "password").strip(" \"");
    
    print "INFO: type: '"+dbtype+"' host: '"+host+"' db: '"+db+"' user: '"+user+"' password: '"+password+"'";
    
    ds = None;
    if dbtype == "PGSQL":
        import psycopg2 as dbapi2;
        print "INFO: psycopg2 drived loaded."
    	ds = dbapi2.connect(host=host, database=db, user=user, password=password);
    elif dbtype == 'MYSQL':
        import MySQLdb as dbapi2;
        print "INFO: MySQLdb driver loaded."
    	ds = dbapi2.connect(host=host, db=db, user=user, passwd=password);
    else:
        print "ERROR: Unrecognised dbtype: "+dbtype;
        exit(1);
        
    cur = ds.cursor();
    cur.execute("""
SELECT userobm_login, domain_name, userobm_password
FROM P_UserObm
INNER JOIN P_Domain ON userobm_domain_id=domain_id
WHERE 
userobm_password_type='PLAIN' AND
userobm_mail_perms=1 AND 
userobm_archive=0 AND
NOT domain_global
ORDER BY domain_name, userobm_login
""");
        
    rows = cur.fetchall();
    cur.close();
    return rows;

# login on the minig backend using the given tuple with login, domain
# & password
def init_minig_index(host, port, row):
    print "INFO: init index for "+row[0]+"@"+row[1]+" on "+host+":"+str(port);
    params = urllib.urlencode({ "login": row[0], "domain": row[1], "password": row[2] });
    headers = { "Content-type": "application/x-www-form-urlencoded" };
    con = httplib.HTTPConnection(host, port);
    try:
        con.request("POST", "/firstIndexing.do", params, headers);
        response = con.getresponse();
        print "INFO:", response.status, response.reason
        data = response.read();
    except Exception, e:
        print "ERROR:", e
    con.close();

def usage():
    print """usage: ./minig_init_index.py <backend_host> <backend_port>
example: ./minig_init_index.py localhost 8081""";

if __name__ == "__main__": 
    if len(sys.argv) != 3:
        usage();
        exit(1);

    rows = fetch_user_passwords();
    for i in range (len(rows)):
        init_minig_index(sys.argv[1], int(sys.argv[2]), rows[i]);
        print "INFO: progress: "+str(i+1)+"/"+str(len(rows));
