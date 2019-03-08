#!/bin/bash

# Provide details for running
cat << EnvInfo
   Ensure the following environment variables configurd for IDE to run appengine:

     DATASTORE_EMULATOR_HOST=localhost:8081  # as per below
     GOOGLE_CLOUD_PROJECT=1

EnvInfo

# Start the datastore emulator
gcloud beta emulators datastore start --no-store-on-disk
