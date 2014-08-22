#!/bin/bash

rm -rf target

curl -sSL 'http://www.stackscaling.com/downloads/package_cronus' | DIR=. appname=cronusmaster version="0.1.`date +%Y%m%d%H%M`" pkgsrc="AgentMaster cronus play-1.2.4" bash

mkdir target
mv *.cronus *.cronus.prop target/

