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
DATE_SUFFIX=`date +%Y%m%d-%H-%M-%S`
RESULT_DIR=/home/$USER/results
mkdir -p ${RESULT_DIR}
RESULT_FILE=${RESULT_DIR}/${SERVICER_NAME}_${TARGET_HOST}_${DATE_SUFFIX}.txt
TESTCASE=${SERVICER_NAME}NioTest
echo "Running test ${TESTCASE} writing results to ${RESULT_FILE}"

# Run the performance test
export MAVEN_OPTS="-Xms3g -Xmx3g"
mvn -o -Dtest=${TESTCASE} -Dtarget.host=${TARGET_HOST} test > ${RESULT_FILE} 

