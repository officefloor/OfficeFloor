#!/bin/bash
set -e
set -x

# Enable running docker in CI
export COMPOSE_INTERACTIVE_NO_CLI=1


# Obtain the script directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


# Obtain latest OfficeFloor code
if [ -d "${DIR}/OfficeFloorFrameworkBenchmarks" ]; then
	rm -rf "${DIR}/OfficeFloorFrameworkBenchmarks"
fi
if [ ! -d "${DIR}/OfficeFloorFrameworkBenchmarks" ]; then
	mkdir "${DIR}/OfficeFloorFrameworkBenchmarks"
fi
cd "${DIR}/OfficeFloorFrameworkBenchmarks"
git clone --depth 1 https://github.com/sagenschneider/FrameworkBenchmarks.git

# Obtain latest TechEmpower code
if [ -d "${DIR}/TechEmpowerFrameworkBenchmarks" ]; then
	rm -rf "${DIR}/TechEmpowerFrameworkBenchmarks"
fi
if [ ! -d "${DIR}/TechEmpowerFrameworkBenchmarks" ]; then
	mkdir "${DIR}/TechEmpowerFrameworkBenchmarks"
fi
cd "${DIR}/TechEmpowerFrameworkBenchmarks"
git clone --depth 1 https://github.com/TechEmpower/FrameworkBenchmarks.git


# Create run directory
if [ ! -d "${DIR}/FrameworkBenchmarks" ]; then
	mkdir "${DIR}/FrameworkBenchmarks"
fi
cp -Rf "${DIR}/TechEmpowerFrameworkBenchmarks/FrameworkBenchmarks" "${DIR}/FrameworkBenchmarks"
if [ ! -d "${DIR}/FrameworkBenchmarks/FrameworkBenchmarks/frameworks/Java/officefloor" ]; then
	rm -rf "${DIR}/FrameworkBenchmarks/FrameworkBenchmarks/frameworks/Java/officefloor"
fi
cp -Rf "${DIR}/OfficeFloorFrameworkBenchmarks/FrameworkBenchmarks/frameworks/Java/officefloor" "${DIR}/FrameworkBenchmarks/FrameworkBenchmarks/frameworks/Java/officefloor"

# Work around for running in Jenkins
cp "${DIR}/OfficeFloorFrameworkBenchmarks/FrameworkBenchmarks/tfb" "${DIR}/FrameworkBenchmarks/FrameworkBenchmarks/tfb"

# Clear results directory (so can find the one results file)
if [ -d "${DIR}FrameworkBenchmarks/FrameworkBenchmarks/results" ]; then
	rm -rf "${DIR}FrameworkBenchmarks/FrameworkBenchmarks/results" 
fi

# Run the comparison
cd "${DIR}/FrameworkBenchmarks/FrameworkBenchmarks"
./tfb --test h2o actix-raw rapidoid-http-fast vertx-postgres vertx-web-postgres officefloor

# Find the latest results directory
RESULTS_DIR=''
for CHECK_DIR in $(ls -t "${DIR}/FrameworkBenchmarks/FrameworkBenchmarks/results"); do
	if [  -z "${RESULTS_DIR}" ]; then
		RESULTS_DIR="${CHECK_DIR}"
	fi
done

# Copy the results to top level
if [  -f "${DIR}/results.json" ]; then
	rm "${DIR}/results.json"
fi
cp "${DIR}/FrameworkBenchmarks/FrameworkBenchmarks/results/${RESULTS_DIR}/results.json" "${DIR}/results.json"
