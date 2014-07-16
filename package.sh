#!/bin/bash

rm -rf target
curl -sS 'https://raw.githubusercontent.com/yubin154/cronusagent/master/agent/scripts/cronus_package/package.sh' | DIR=. appName=cronusmaster version="0.1.`date +%Y%m%d%H%M`" platform=all bash
mkdir target
mv *.cronus *.cronus.prop target/

