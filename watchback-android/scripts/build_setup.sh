#!/usr/bin/env bash

#setting up SDK paths
#rm local.properties || true
echo -e "sdk.dir=${ANDROID_HOME}" > local.properties

#command to download android dependencies
./gradlew --stacktrace --info androidDependencies || true