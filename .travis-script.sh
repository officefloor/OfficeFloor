#!/bin/bash
set -x
set -e

# Run standard Travis script
cd officefloor/bom
echo "Standard Travis script stage"
mvn test -B -Dmaven.main.skip

# Ensure backwards compatibility for Eclipse
cd ../editor
echo "Backwards compability for Eclipse"
# Latest already tested by default
mvn clean install -P PHOTON.target
mvn clean install -P OXYGEN.target
