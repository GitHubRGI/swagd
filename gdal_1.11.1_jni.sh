#!/bin/sh
set -e
# check to see if the gdal_1.11.1_jni folder is empty
if [ ! -d "$HOME/gdal/lib" ]; then
    wget http://download.osgeo.org/gdal/1.11.1/gdal-1.11.1.tar.gz;
    tar xzf gdal-1.11.1.tar.gz;
    ls -al $HOME/gdal;
#    cd gdal-1.11.1 && ./configure --prefix=$HOME/gdal && make && make install;
#    cd $HOME/gdal/lib/swig/java && make;
else
    echo 'Using cached directory.';
fi
