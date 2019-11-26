#!/bin/bash
set -euxo pipefail

##############################################################################
##
##  Travis CI test script
##
##############################################################################

mvn -pl models install
mvn package

docker pull open-liberty

docker build -t system:1.0-SNAPSHOT system/.
docker build -t inventory:1.0-SNAPSHOT inventory/.
docker build -t job:1.0-SNAPSHOT job/.
docker build -t gateway:1.0-SNAPSHOT gateway/.

./scripts/start-app

sleep 300

jobCount="$(curl --silent http://localhost:8080/api/jobs | jq -r '.count')"
jobStatus="$(curl --write-out "%{http_code}\n" --silent --output /dev/null "http://localhost:8080/api/jobs")"

if [ "$jobStatus" == "200" ] && [ "$jobCount" == "0" ]
then
  echo ENDPOINT OK
else
  echo job status:
  echo "$jobStatus"
  echo job count:
  echo "$jobCount"
  echo ENDPOINT
  exit 1
fi

./scripts/stop-app