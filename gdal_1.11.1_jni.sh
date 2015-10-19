#!/bin/sh
set -e
# check to see if the gdal_1.11.1_jni folder is empty
if [ ! -d "$HOME/gdal/swig" ]; then
    wget http://download.osgeo.org/gdal/1.11.1/gdal-1.11.1.tar.gz;
    tar xzvf gdal-1.11.1.tar.gz;
    cd gdal-1.11.1 && configure --prefix=$HOME/gdal && make && make install;
    cd $HOME/gdal/swig/java && make;
else
    echo 'Using cached directory.';
fi
