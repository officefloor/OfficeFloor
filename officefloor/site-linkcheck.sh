#!/bin/bash
set -e
set -x

mvn -DskipTests clean install post-site
mvn site:stage
cp -R target/site/apidocs/ target/staging/
rm -rf target/site
mv target/staging/ target/site
rm -rf target/linkcheck
rm -rf target/site/linkcheck.html
mvn -N post-site
google-chrome target/site/linkcheck.html &