#!/bin/bash
set -x
set -e

if [ -z "${CUSTOM_MVN_VERSION}" ]; then
	# No customisation of maven
	echo "Using default Maven"
else
	# Install custom version of maven if specified
	echo "Downloading Maven ${CUSTOM_MVN_VERSION} ..."
	wget https://archive.apache.org/dist/maven/maven-3/${CUSTOM_MVN_VERSION}/binaries/apache-maven-${CUSTOM_MVN_VERSION}-bin.zip || travis_terminate 1
	unzip -qq apache-maven-${CUSTOM_MVN_VERSION}-bin.zip || travis_terminate 1
	export M2_HOME=$PWD/apache-maven-${CUSTOM_MVN_VERSION}
	export PATH=$M2_HOME/bin:$PATH
fi
mvn -version

