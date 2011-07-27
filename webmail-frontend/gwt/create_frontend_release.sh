#!/bin/bash

build_version=`svnversion`

ant clean dist

cd dist

jar cvf ../minig-frontend-0.${build_version}.war *
