#!/bin/bash

# Obtains the java.home property value
JAVA_HOME=`java -XshowSettings:properties -version 2>&1 | grep java.home | awk -F'=' '{print $2}'`

# Strip off possible jre directory
JAVA_HOME=${JAVA_HOME%/jre}

# Export JAVA_HOME (ensure script run with source set_java_home.sh)
echo "Setting JAVA_HOME to $JAVA_HOME"
export JAVA_HOME
