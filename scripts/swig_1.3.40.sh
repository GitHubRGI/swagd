#!/bin/bash
set -e
# check to see if the swig_1.3.40 folder is empty
if [ ! -d "$HOME/swig_1.3.40/lib" ]; then
    # get swig 1.3.40 source
    wget https://sourceforge.net/projects/swig/files/swig/swig-1.3.40/swig-1.3.40.tar.gz;
    # quietly untar
    tar xzf swig-1.3.40.tar.gz;
    # make swig
    echo 'Making SWIG 1.3.40...';
    cd swig-1.3.40 && ./configure --prefix=$HOME/swig_1.3.40 && make && make install;
else
    echo 'Using cached directory.';
fi
