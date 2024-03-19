#!/bin/zsh
../gradlew -p . clean build
docker build -t temperature_anomaly_analyzer -f Dockerfile .
