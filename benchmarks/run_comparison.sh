#!/bin/bash
set -e
set -x

# Enable running docker in CI
export COMPOSE_INTERACTIVE_NO_CLI=1


# Obtain the script directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Clear any previous results
if [  -f "${DIR}/results.txt" ]; then
	rm "${DIR}/results.txt"
fi
if [ -f "${DIR}/results.zip" ]; then
	rm "${DIR}/results.zip"
fi


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
if [ -d "${DIR}/FrameworkBenchmarks/FrameworkBenchmarks/frameworks/Java/officefloor" ]; then
	rm -rf "${DIR}/FrameworkBenchmarks/FrameworkBenchmarks/frameworks/Java/officefloor"
fi
cp -Rf "${DIR}/OfficeFloorFrameworkBenchmarks/FrameworkBenchmarks/frameworks/Java/officefloor" "${DIR}/FrameworkBenchmarks/FrameworkBenchmarks/frameworks/Java/"

# Work around for running in Jenkins
cp "${DIR}/OfficeFloorFrameworkBenchmarks/FrameworkBenchmarks/tfb" "${DIR}/FrameworkBenchmarks/FrameworkBenchmarks/tfb"

# Clear results directory (so can find the one results file)
if [ -d "${DIR}FrameworkBenchmarks/FrameworkBenchmarks/results" ]; then
	rm -rf "${DIR}FrameworkBenchmarks/FrameworkBenchmarks/results" 
fi

# Ensure passing tests
cd "${DIR}/OfficeFloorFrameworkBenchmarks/FrameworkBenchmarks/frameworks/Java/officefloor/src"
mvn clean install
cd "${DIR}/test"
mvn clean install

# Run the comparison (don't fail build if run fails - as could be just temporary Internet network issue)
cd "${DIR}/FrameworkBenchmarks/FrameworkBenchmarks"
./tfb --clean
./tfb --test rapidoid-http-fast netty vertx-postgres vertx-web-postgres officefloor officefloor-raw officefloor-micro officefloor-thread_affinity officefloor-tpr officefloor-netty officefloor-rapidoid || true

# Find the latest results directory
RESULTS_DIR=''
for CHECK_DIR in $(ls -t "${DIR}/FrameworkBenchmarks/FrameworkBenchmarks/results"); do
	if [  -z "${RESULTS_DIR}" ]; then
		RESULTS_DIR="${CHECK_DIR}"
	fi
done

# Move the results to top level (avoids duplicating results in zip)
if [  -f "${DIR}/results.json" ]; then
	rm "${DIR}/results.json"
fi
cp "${DIR}/FrameworkBenchmarks/FrameworkBenchmarks/results/${RESULTS_DIR}/results.json" "${DIR}/results.txt"

# Create zip of results directory (to aid identifying causes of failure)
if [ -f "${DIR}/results.zip" ]; then
	rm "${DIR}/results.zip"
fi
zip -r "${DIR}/results.zip" "${DIR}/FrameworkBenchmarks/FrameworkBenchmarks/results/${RESULTS_DIR}/"


