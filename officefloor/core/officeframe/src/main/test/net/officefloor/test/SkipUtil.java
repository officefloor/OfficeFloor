/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.test;

/**
 * Functionality for skipping tests.
 * 
 * @author Daniel Sagenschneider
 */
public class SkipUtil {

	/**
	 * Skip test {@link System} property.
	 */
	public static final String SKIP_STRESS_SYSTEM_PROPERTY = "officefloor.skip.stress.tests";

	/**
	 * Skip test environment variable.
	 */
	public static final String SKIP_STRESS_ENVIRONMENT_VARIABLE = "OFFICEFLOOR_SKIP_STRESS_TESTS";

	/**
	 * Docker available {@link System} property.
	 */
	public static final String DOCKER_AVAILABLE_SYSTEM_PROPERTY = "officefloor.docker.available";

	/**
	 * Docker available environment variable.
	 */
	public static final String DOCKER_AVAILABLE_ENVIRONMENT_VARIABLE = "OFFICEFLOOR_DOCKER_AVAILABLE";

	/**
	 * GCloud {@link System} property.
	 */
	public static final String GCLOUD_AVAILABLE_SYSTEM_PROPERTY = "officefloor.gcloud.available";

	/**
	 * GCloud environment variable.
	 */
	public static final String GCLOUD_AVAILABLE_ENVIRONMENT_VARIABLE = "OFFICEFLOOR_GCLOUD_AVAILABLE";

	/**
	 * AWS {@link System} property.
	 */
	public static final String AWS_AVAILABLE_SYSTEM_PROPERTY = "officefloor.aws.available";

	/**
	 * AWS environment variable.
	 */
	public static final String AWS_AVAILABLE_ENVIRONMENT_VARIABLE = "OFFICEFLOOR_AWS_AVAILABLE";

	/**
	 * <p>
	 * Indicates if not to run stress tests.
	 * <p>
	 * Stress tests should normally be run, but in cases of quick unit testing
	 * running for functionality the stress tests can reduce turn around time and
	 * subsequently the effectiveness of the tests. This is therefore provided to
	 * maintain effectiveness of unit tests.
	 * <p>
	 * Furthermore, builds time out on Travis so avoid running.
	 * 
	 * @return <code>true</code> to ignore doing a stress test.
	 */
	public static boolean isSkipStressTests() {
		return isSkipTests(SKIP_STRESS_SYSTEM_PROPERTY, SKIP_STRESS_ENVIRONMENT_VARIABLE, false, "Stress");
	}

	/**
	 * <p>
	 * Indicates if not to run tests using docker.
	 * <p>
	 * Some environments do not support docker, so this enables disabling these
	 * tests.
	 * 
	 * @return <code>true</code> to ignore doing a docker test.
	 */
	public static boolean isSkipTestsUsingDocker() {
		return isSkipTests(DOCKER_AVAILABLE_SYSTEM_PROPERTY, DOCKER_AVAILABLE_ENVIRONMENT_VARIABLE, true, null);
	}

	/**
	 * <p>
	 * Indicates if not to run tests using GCloud (Google Cloud).
	 * <p>
	 * Some environments do not have GCloud available, so this enables disabling
	 * these tests.
	 * 
	 * @return <code>true</code> to ignore doing a GCloud test.
	 */
	public static boolean isSkipTestsUsingGCloud() {
		return isSkipTests(GCLOUD_AVAILABLE_SYSTEM_PROPERTY, GCLOUD_AVAILABLE_ENVIRONMENT_VARIABLE, true, null);
	}

	/**
	 * <p>
	 * Indicates if not to run tests using AWS and local services like SAM.
	 * <p>
	 * Some environments do not have AWS available, so this enables disabling these
	 * tests.
	 * 
	 * @return <code>true</code> to ignore doing a AWS test.
	 */
	public static boolean isSkipTestsUsingAws() {
		return isSkipTests(AWS_AVAILABLE_SYSTEM_PROPERTY, AWS_AVAILABLE_ENVIRONMENT_VARIABLE, true, null);
	}

	/**
	 * Determines if skips the tests.
	 * 
	 * @param systemPropertyName      {@link System} property name.
	 * @param environmentVariableName Environment variable name.
	 * @param isNegatePropertyValue   Indicates whether to negate the property value
	 *                                to determine if skip.
	 * @param shortCutName            Optional short cut name.
	 * @return <code>true</code> to ignore doing the tests.
	 */
	private static boolean isSkipTests(String systemPropertyName, String environmentVariableName,
			boolean isNegatePropertyValue, String shortCutName) {

		// Determine based on short cut
		if ((shortCutName != null) && System.getProperties().containsKey("skip" + shortCutName)) {
			return true;
		}

		// Determine based on property
		String value = System.getProperty(systemPropertyName);
		if (value == null) {
			value = System.getenv(environmentVariableName);
		}
		if (value == null) {
			return false;
		} else {
			boolean isValue = Boolean.parseBoolean(value);
			return isNegatePropertyValue ? !isValue : isValue;
		}
	}

	/**
	 * All access via static methods.
	 */
	protected SkipUtil() {
		// All access via static methods
	}

}
