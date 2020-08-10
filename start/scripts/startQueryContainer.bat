start /b docker run -d ^
  -e InventoryClient_mp_rest_url=http://inventory:9085 ^
  -e INVENTORY_BASE_URI=http://inventory:9085 ^
  -e MP_MESSAGING_CONNECTOR_LIBERTY_KAFKA_BOOTSTRAP_SERVERS=%KAFKA_SERVER% ^
  -p 9080:9080 ^
  --network=%NETWORK% ^
  --name=query ^
  --rm ^
  query:1.0-SNAPSHOT 