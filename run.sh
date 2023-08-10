#!/bin/sh
if [ -e config.json ]; then
    echo "config.json exists, proceeding to run the application..."
    sbt "runMain EventPublish"
else
    echo "Error: config.json file not found. Please make sure the file exists and try again."
fi