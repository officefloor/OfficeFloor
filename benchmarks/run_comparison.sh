#!/bin/bash
set -e
set -x

# Clear previous run
if [ -d "/tmp/FrameworkBenchmarks" ]; then
        rm -rf /tmp/FrameworkBenchmarks
fi
cd /tmp

# Obtain the current OfficeFloor configuration
git clone --depth 1 git@github.com:sagenschneider/FrameworkBenchmarks.git

# Pull in latest Framework (keeps other frameworks up to date)
cd FrameworkBenchmarks
git remote add techempower git@github.com:TechEmpower/FrameworkBenchmarks.git
git pull techempower master

# Run comparisions
./tfb --test h2o actix-raw rapidoid-http-fast vertx-postgres vertx-web-postgres
