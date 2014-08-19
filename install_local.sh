#!/bin/bash -ae

if [[ -z "$1" ]]; then
  env="production"
else
  env="$1"
fi
echo "deploy environment $env"

echo "package cronus"
./package.sh

cd target
PKG=`ls cronusmaster*.cronus`
cp cronusmaster*.cronus /var/cronus/software/packages/
chmod 666 /var/cronus/software/packages/cronusmaster*.cronus
echo "will install $PKG"

CMD_BODY="{\"package\": [\"http://host/$PKG\"], \"manifest\": \"0.0.1\", \"env\": \"$env\"}"
echo "use cronus cmd $CMD_BODY"
curl -k -H "content-type:application/json" -X POST -d "$CMD_BODY" https://localhost:12020/services/cronusmaster/action/deployservice
cd ..
