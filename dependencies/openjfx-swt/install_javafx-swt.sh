#!/bin/bash

# Ensure file available for upload
if [  ! -f "javafx-swt.jar" ]; then
	RED='\033[0;31m'
	NC='\033[0m'
	echo -e "${RED}ERROR: please download javafx-swt.jar from http://openjfx.io ${NC}" >&2
	exit 1
fi

# Ensure create account
echo "Ensure the following is run to login:"
echo ""
echo "    ssh -t sagenschneider@shell.sourceforge.net create"
echo ""
echo 'Press [enter] to start upload'
read

# Run the install
echo "Uploading..."
mvn org.apache.maven.plugins:maven-deploy-plugin:deploy-file \
	-Durl=scp://sagenschneider@shell.sourceforge.net:/home/frs/project/officefloor/build/maven2 \
	-Drepository=repo.officefloor.sf.net \
	-Dfile=javafx-swt.jar \
	-DgroupId=org.openjfx \
	-DartifactId=javafx-swt \
	-Dversion=11
