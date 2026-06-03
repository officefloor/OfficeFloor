#!/bin/bash

# Ensure JAVA_HOME set (for JavaDoc)
if [[ -z "$JAVA_HOME" ]]; then
	>&2 echo "ERROR: JAVA_HOME not specified"
	exit 1
fi

# Ensure lychee is available - install from https://lychee.cli.rs/
if ! command -v lychee &> /dev/null; then
	>&2 echo "ERROR: lychee not found. Install from https://lychee.cli.rs/"
	exit 1
fi

# Configure to fail at any step (with details logged)
set -e
set -x

# Build everything (required for javadoc compilation)
mvn -DskipTests -DskipITs clean install

# Generate full site for all modules including javadoc and @VERSION/@GITHUB fixes
mvn -Dofficefloor-deploy=site post-site

# Stage the generated site and include javadoc
mvn site:stage -P site-link-check
cp -R target/site/apidocs/ target/staging/

# Run lychee linkcheck against staged site (config in .lychee.toml)
# --root-dir resolves root-relative paths (e.g. /js/...) relative to the staging root
STAGING_DIR="$(pwd)/target/staging"
lychee --root-dir "${STAGING_DIR}" "${STAGING_DIR}/**/*.html"

# Extract all unique external links from the staged site for visual verification
EXTERNAL_LINKS_REPORT=target/staging/external-links.txt
echo "=== External domains ===" > "${EXTERNAL_LINKS_REPORT}"
grep -roh 'href="https\?://[^"]*"\|src="https\?://[^"]*"' target/staging/ \
	| sed 's/href="//;s/src="//;s/"//' \
	| grep -v '^http://localhost' \
	| sed 's|^\(https\?://[^/]*\).*|\1|' \
	| sort -u \
	>> "${EXTERNAL_LINKS_REPORT}"
echo "" >> "${EXTERNAL_LINKS_REPORT}"
echo "=== All unique external links ===" >> "${EXTERNAL_LINKS_REPORT}"
grep -roh 'href="https\?://[^"]*"\|src="https\?://[^"]*"' target/staging/ \
	| sed 's/href="//;s/src="//;s/"//' \
	| grep -v '^http://localhost' \
	| sort -u \
	>> "${EXTERNAL_LINKS_REPORT}"
echo "External links report: ${EXTERNAL_LINKS_REPORT}"
