#!/bin/bash

set -e

# Check to see if the swig folder already exists, skip installing if so
if [ ! -d "$HOME/swig-1.3.40/bin"]; then
    # Install swig 1.3.40
    wget "http://downloads.sourceforge.net/project/swig/swig/swig-1.3.40/swig-1.3.40.tar.gz"
    tar xzf swig-1.3.40.tar.gz
    pushd swig-1.3.40
    ./configure --prefix=$HOME/swig_1.3.40
    make && make install && popd
else
    echo 'Using cached directory.';
fi
