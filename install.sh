#!/bin/bash

# check and install agent if necessary
agent_url="https://localhost:12020/agent/ValidateInternals"
status=$(curl -s -k -L --head -o /dev/null -w "%{http_code}" $agent_url)
echo "check agent status=$status"
# check if the status has a non-zero length
if [ "$status" == 000 ]; then
   echo "Agent not installed, installing with dev mode..."
   wget -qO- 'http://www.stackscaling.com/downloads/install_agent' | sudo dev=true bash
   sleep 2
   echo "Done"
fi
echo ""

echo "clean any previous cronusmaster installation"
curl -sSk -H "content-type:application/json" -X DELETE https://localhost:12020/services/cronusmaster/action/cleanup
sleep 2
echo "Done"
echo ""

if [[ -z "$1" ]]; then
  env="prod"
else
  env="$1"
fi
echo "deploy to environment $env"

daemon=""
if [[ ! -z "$2" ]]; then
  daemon=", \"daemon\": \"$2\""
fi

echo "package cronus"
./package.sh

cd target
PKG=`ls cronusmaster*.cronus`
cp cronusmaster*.cronus /var/cronus/software/packages/
chmod 666 /var/cronus/software/packages/cronusmaster*.cronus
echo "will install $PKG"

CMD_BODY="{\"package\": [\"http://host/$PKG\"], \"manifest\": \"0.0.1\", \"env\": \"$env\" $daemon}"
echo "use cronus cmd $CMD_BODY"
curl -k -H "content-type:application/json" -X POST -d "$CMD_BODY" https://localhost:12020/services/cronusmaster/action/deploy
echo 

echo "verify installation"
validation_url="http://localhost:9000/"
# poll until the application started completely
while true;
do
   export status=$(curl -s -L --head -o /dev/null -w "%{http_code}" $validation_url)
   # check if the status has a non-zero length
   if [ "$status" == 000 ]
   then
      echo "Application has yet started. status=$status"
      sleep 1
   else
      echo "Application has started!! status=$status"
      break
   fi
done

cd ..
