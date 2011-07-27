#!/bin/bash

source `whoami`.build.properties

rm -fr soyc

java -cp ${gwt}/gwt-soyc-vis.jar:${gwt}/gwt-dev-linux.jar com.google.gwt.soyc.SoycDashboard -resources ${gwt}/gwt-soyc-vis.jar -out soyc extra/minig/soycReport/stories0.xml.gz
