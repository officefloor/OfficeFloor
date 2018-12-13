#!/bin/bash
set -x
set -e

# Run standard Travis script
cd officefloor/bom
echo "Standard Travis script stage"
mvn test -B

# Ensure backwards compatibility for Eclipse
cd ../eclipse
echo "Backwards compability for Eclipse"
# PHOTON.target already tested by default
mvn clean install -P OXYGEN.target
mvn clean install -P NEON.target
