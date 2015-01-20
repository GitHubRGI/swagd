#!/bin/bash
mvn install:install-file -Dfile="Gdal2Tiles/lib/gdal.jar" -DgroupId=org.gdal -DartifactId=gdal -Dversion=1.11.0 -Dpackaging=jar
mvn install:install-file -Dfile="GeoViewer/lib/JMapViewer-1.05.jar" -DgroupId=org.openstreetmap -DartifactId=JMapViewer -Dversion=1.05 -Dpackaging=jar
mvn clean compile test
