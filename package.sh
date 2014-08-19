#!/bin/bash

rm -rf target

curl -sS 'https://raw.githubusercontent.com/yubin154/cronuspackages/master/common_scripts/package.sh' | DIR=. appname=cronusmaster version="0.1.`date +%Y%m%d%H%M`" pkgsrc="AgentMaster cronus play-1.2.4" bash

mkdir target
mv *.cronus *.cronus.prop target/

