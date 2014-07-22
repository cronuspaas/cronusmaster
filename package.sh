#!/bin/bash

rm -rf target
curl -sS 'https://raw.githubusercontent.com/yubin154/cronuspackages/master/common_scripts/package.sh' | DIR=. bash
mkdir target
mv *.cronus *.cronus.prop target/

