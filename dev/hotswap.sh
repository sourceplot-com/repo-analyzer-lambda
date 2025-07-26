#!/usr/bin/env bash

echo "Building and hot-swapping..."
./gradlew clean shadowJar

echo "Hot-swapping..."
aws lambda update-function-code \
    --function-name sourceplot-repo-analyzer \
    --zip-file fileb://build/libs/repo-analyzer-lambda-all.jar
