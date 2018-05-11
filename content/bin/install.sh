#!/bin/sh

#####
# ./install.sh ROOT LOCATION_JAVA_EXEC
#
./kick.sh $1 $2
chmod 755 $1/bin/*
./setup.sh
chmod 755 $1/bin/*
