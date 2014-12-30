#!/bin/bash

DIR=$(cd "$(dirname "$0")/.."; pwd)
cd $DIR

# check and install agent if necessary
check_agent () {
   agent_url="https://localhost:12020/agent/ValidateInternals"
   status=$(curl -s -k -L --head -o /dev/null -w "%{http_code}" $agent_url)
   echo "check agent status=$status"
   # check if the status has a non-zero length
   if [ "$status" == 000 ]; then
      if [ -z "$1" ]; then
         echo "Agent not installed, installing with dev mode..."
         wget -qO- 'http://cronuspaas.github.io/downloads/install_agent' | sudo dev=true bash
         sleep 2
         echo "Done"
      else
         fatal_error $1
      fi
   fi
   echo ""
}

# Log and exit
fatal_error () {
   echo $1
   exit -1
}

check_agent
check_agent "Cronus agent missing or installation failed, abort"

# cleanup all services running locally on the agent
echo "clean all local services"
echo "agent_auth=$agent_auth"
curl -sSk -H "content-type:application/json" -H "Authorization:Basic $agent_auth" -X POST https://localhost:12020/agent/cleanup
sleep 2
echo "Done"
echo ""

# define environment to deploy cronusmaster in, default "prod"
if [[ -z "$1" ]]; then
  env="prod"
else
  env="$1"
fi
echo "deploy to environment $env"

# define whether to run cronusmaster in daemon mode, default to daemon
daemon=""
if [[ ! -z "$2" ]]; then
  daemon=", \"daemon\": \"$2\""
fi

echo "package cronus"
$DIR/scripts/package.sh

cd $DIR/target_cronus
PKG=`ls cronusmaster*.cronus`
cp cronusmaster*.cronus /var/cronus/software/packages/
chmod 666 /var/cronus/software/packages/cronusmaster*.cronus
echo "will install $PKG"

CMD_BODY="{\"package\": [\"http://host/$PKG\"], \"env\": \"$env\" $daemon}"
echo "use cronus cmd $CMD_BODY"
curl -k -H "content-type:application/json" -H "Authorization:Basic $agent_auth" -X POST -d "$CMD_BODY" https://localhost:12020/services/cronusmaster/action/deploy
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
