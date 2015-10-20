#!/bin/sh
set -e
if [ ! -e "/usr/lib/jvm/java-8-oracle/include/jni.h" ]; then
    exit 1
fi
# check to see if the gdal_1.11.1_jni folder is empty
if [ ! -d "$HOME/gdal/lib" ]; then
    # get gdal from osgeo
    wget http://download.osgeo.org/gdal/1.11.1/gdal-1.11.1.tar.gz;
    # untar the source quietly
    tar xzf gdal-1.11.1.tar.gz;
    # configure and make/install gdal
    echo 'Making GDAL...';
    cd gdal-1.11.1 && ./configure --prefix=$HOME/gdal && make && make install;
    # make the java bindings
    echo 'Making SWIG bindings for Java...';
    # Set java env vars
    export JAVA_HOME=/usr/lib/jvm/java-8-oracle
    export JAVA_INCLUDE=-I$JAVA_HOME/include:$JAVA_INCLUDE;
    export JAVA_INCLUDE=-I$JAVA_HOME/include/linux:$JAVA_INCLUDE;
    cd swig/java && make;
    # export the java bindings to $HOME/gdal
    cd .. && mkdir -p $HOME/gdal/swig && cp -r java $HOME/gdal/swig;
else
    echo 'Using cached directory.';
fi
