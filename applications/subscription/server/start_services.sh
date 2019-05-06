#!/bin/bash

# Provide project name (so avoid login and accidental releasing)
export CLOUDSDK_CORE_PROJECT=subscription

# Specify location of emulator
export DATASTORE_EMULATOR_HOST='localhost:18081'

# Provide details for running
cat << EnvInfo
   Ensure the following environment variables configurd for IDE to run appengine:

     DATASTORE_EMULATOR_HOST=${DATASTORE_EMULATOR_HOST}  # as per below
     GOOGLE_CLOUD_PROJECT=${CLOUDSDK_CORE_PROJECT}

EnvInfo

# Start the datastore emulator
gcloud beta emulators datastore start --no-store-on-disk --host-port=${DATASTORE_EMULATOR_HOST}
