#!/bin/bash

rm -fr built_archive_test && mkdir built_archive_test

pushd built_archive_test >/dev/null 2>&1
tar xfj ../minig-backend-svn-linux.tar.bz2 >/dev/null 2>&1
time ./minig-backend/webmail-backend
popd >/dev/null 2>&1
rm -fr built_archive_test
