#!/bin/bash
set -euxo pipefail
./mvnw -version

./mvnw -ntp -pl models clean install
./mvnw -ntp -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -q clean package

./mvnw -ntp -pl system verify
./mvnw -ntp -pl inventory verify
./mvnw -ntp -pl query verify

./scripts/buildImages.sh
./scripts/startContainers.sh

sleep 180

docker logs inventory
docker logs query

systemCPULoad="$(curl --write-out "%{http_code}" --silent --output /dev/null "http://localhost:9080/query/systemLoad")"

if [ "$systemCPULoad" == "200" ]
then
  echo SystemInventory OK
else
  echo System Inventory status:
  echo "$systemCPULoad"
  echo ENDPOINT
  exit 1
fi

./scripts/stopContainers.sh
