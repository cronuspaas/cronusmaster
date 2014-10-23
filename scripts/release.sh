#!/bin/bash
# DIR: root package directory, default current working dir
# appname: name of the app
# version: version of the app
# pkgsrc: what to include in the package, default all non hidden files and dirs
# platform: platform (arch) x86 or x64
# 
# Package $appname-$version.$platform.cronus is in folder target_cronus

usage() {
    echo "release cronusmaster"
    echo "  ./release.sh version"
    echo "  e.g. ./release.sh 0.1.1"
    exit 1;
}

if [[ -z $1 ]]; then
    usage
fi

if [[ $1 == "-h" ]]; then
    usage
fi


DIR=$(cd "$(dirname "$0")/.."; pwd) # default root dir

cd $DIR
tar -h --preserve-permissions --ignore-failed-read -czf cronusmaster-$1.gz --directory $DIR AgentMaster cronus scripts

mv cronusmaster-$1.gz releases/

