#!/bin/sh
set -e
# check to see if the gdal_1.11.1_jni folder is empty
if [ ! -d "$HOME/gdal_2.0.1/lib" ]; then
    # get gdal from osgeo
    wget http://download.osgeo.org/gdal/2.0.1/gdal-2.0.1.tar.gz;
    # untar the source quietly
    tar xzf gdal-2.0.1.tar.gz;
    cp java.opt gdal-2.0.1/swig/java
    # configure and make/install gdal
    echo 'Making GDAL...';
    cd gdal-2.0.1 && ./configure --prefix=$HOME/gdal_2.0.1 --without-libtool && make && make install;
    # make the java bindings
    echo 'Making SWIG bindings for Java...';
    # Set java env vars
    cd swig/java && make -I/usr/lib/jvm/java-8-oracle/include -I/usr/lib/jvm/java-8-oracle/include/linux;
    # export the java bindings to $HOME/gdal
    cd .. && mkdir -p $HOME/gdal_2.0.1/swig && cp -r java $HOME/gdal_2.0.1/swig;
    ln -s /usr/lib/libproj.so.0.7.0 $HOME/gdal_2.0.1/lib/libproj.so
else
    echo 'Using cached directory.';
fi
