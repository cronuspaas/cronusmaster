#!/bin/bash
# this download and unpack play 1.x container
# change the play download link for custom version of the container 

DIR=$(cd "$(dirname "$0")/.."; pwd)
cd $DIR

TMPFILE="tempfile"
wget -O $TMPFILE "http://downloads.typesafe.com/play/1.2.7/play-1.2.7.zip"
unzip $TMPFILE
mv play-1.2.7 play_runtime
rm $TMPFILE
