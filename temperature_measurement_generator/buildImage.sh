#!/bin/zsh

gradle clean shadowJar

docker build -t temperature_measurement_generator -f Dockerfile_java .
