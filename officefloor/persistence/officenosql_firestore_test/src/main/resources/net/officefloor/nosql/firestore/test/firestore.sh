#!/bin/bash
set -xe
gcloud beta emulators firestore start --host-port="0.0.0.0:8080"