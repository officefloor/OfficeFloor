#!/bin/sh

######################################################
# Runs the performance test for a particular server
#
# Ensure run ConfigureLinux.sh before running.
#
######################################################

# Fail on error
set -e

# Error message
USAGE_MSG="$0 <servicer prefix> [<target host name>]"

# Ensure have servicer name
SERVICER_NAME=$1
if [ -z $SERVICER_NAME ]; then
	echo "$USAGE_MSG"
	exit 1
fi

# Ensure have target host name
TARGET_HOST=$2
if [ -z $TARGET_HOST ]; then
	# Use default target host
	TARGET_HOST=192.168.0.50
fi


# Log details of running
RESULT_FILE=/home/$USER/${SERVICER_NAME}_${TARGET_HOST}.txt
TESTCASE=${SERVICER_NAME}NioTest
echo "Running test ${TESTCASE} writing results to ${RESULT_FILE}"

# Run the performance test
mvn -Dtest=${TESTCASE} test > ${RESULT_FILE} 
