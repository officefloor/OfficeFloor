#!/bin/bash
set -x
set -e

# Default Travix install except avoids archetype integration test also
cd officefloor/bom
mvn clean install -DskipTests=true -DskipITs -Dmaven.javadoc.skip=true -Darchetype.test.skip=true -B -V -e
