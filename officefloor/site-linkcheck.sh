#!/bin/bash

# Ensure JAVA_HOME set (for JavaDoc)
if [[ -z "$JAVA_HOME" ]]; then
	>&2 echo "ERROR: JAVA_HOME not specified"  
	exit 1
fi

# Configure to fail at any step (with details logged)
set -e
set -x

mvn -DskipTests -DskipITs clean install
mvn post-site
mvn site:stage -P site-link-check
cp -R target/site/apidocs/ target/staging/
rm -rf target/site
mv target/staging/ target/site
rm -rf target/linkcheck
rm -rf target/site/linkcheck.html
# TODO: provide -N once NPE issue resolved
mvn post-site
google-chrome target/site/linkcheck.html &
