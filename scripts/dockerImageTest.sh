#!/bin/bash
while getopts t:d: flag;
do
    case "${flag}" in
        t) DATE="${OPTARG}" ;;
        d) DRIVER="${OPTARG}" ;;
        *) : ;;
    esac
done

echo "Testing daily Docker image"

sed -i "\#<artifactId>liberty-maven-plugin</artifactId>#a<configuration><install><runtimeUrl>https://public.dhe.ibm.com/ibmdl/export/pub/software/openliberty/runtime/nightly/$DATE/$DRIVER</runtimeUrl></install></configuration>" system/pom.xml inventory/pom.xml query/pom.xml
cat system/pom.xml inventory/pom.xml query/pom.xml

sed -i "s;FROM openliberty/open-liberty:full-java11-openj9-ubi;FROM openliberty/daily:latest;g" system/Dockerfile inventory/Dockerfile query/Dockerfile
cat system/Dockerfile inventory/Dockerfile query/Dockerfile

docker pull "openliberty/daily:latest"

../scripts/testApp.sh
