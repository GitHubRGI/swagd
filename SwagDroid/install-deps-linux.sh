#!/bin/bash
mvn install:install-file -Dfile=libs/sqldroid-1.0.4-SNAPSHOT.jar \
    -DgroupId=org.sqldroid -DartifactId=sqldroid -Dversion=1.0.4-SNAPSHOT \
    -Dpackaging=jar
