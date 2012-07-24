#!/bin/sh

######################################################
# Runs the the particular server.
#
# Ensure run ConfigureLinux.sh before running.
#
######################################################

# Fail on error
set -e

# Error message
USAGE_MSG="$0 <servicer prefix>"

# Ensure have servicer name
SERVICER_NAME=$1
if [ -z $SERVICER_NAME ]; then
	echo "$USAGE_MSG"
	exit 1
fi


# Run the servicer
mvn exec:exec -Dexec.executable="java" -Dexec.args="-classpath %classpath net.officefloor.tutorials.performance.RunServicer ${SERVICER_NAME}"
