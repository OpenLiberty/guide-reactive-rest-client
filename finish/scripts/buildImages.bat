@ECHO Starting Scripts
@ECHO OFF

@REM start /b docker pull bitnami/zookeeper:3
start /b docker pull bitnami/kafka:latest

start /b docker build -q -t system:1.0-SNAPSHOT system\.
start /b docker build -q -t inventory:1.0-SNAPSHOT inventory\.
start /b docker build -q -t query:1.0-SNAPSHOT query\.
