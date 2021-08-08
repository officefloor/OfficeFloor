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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.test.system.SystemPropertiesExtension;

/**
 * Ensure skip tests based on system properties.
 * 
 * @author Daniel Sagenschneider
 */
public class SystemPropertyIgnoreTest {

	private static List<String> testsRun = new LinkedList<>();

	@RegisterExtension
	public static final SystemPropertiesExtension systemProperties = new SystemPropertiesExtension(
			SkipUtil.SKIP_STRESS_SYSTEM_PROPERTY, "true", SkipUtil.DOCKER_AVAILABLE_SYSTEM_PROPERTY, "false",
			SkipUtil.GCLOUD_AVAILABLE_SYSTEM_PROPERTY, "false", SkipUtil.AWS_AVAILABLE_SYSTEM_PROPERTY, "false");

	@BeforeAll
	public static void clearTests() {
		testsRun.clear();
	}

	@Test
	public void shouldRun(TestInfo testInfo) {
		testsRun.add(testInfo.getTestMethod().get().getName());
	}

	@StressTest
	public void ignoreStress(TestInfo testInfo) {
		testsRun.add(testInfo.getTestMethod().get().getName());
	}

	@UsesDockerTest
	public void ignoreDocker(TestInfo testInfo) {
		testsRun.add(testInfo.getTestMethod().get().getName());
	}

	@UsesGCloudTest
	public void ignoreGCloud(TestInfo testInfo) {
		testsRun.add(testInfo.getTestMethod().get().getName());
	}

	@UsesAwsTest
	public void ignoreAws(TestInfo testInfo) {
		testsRun.add(testInfo.getTestMethod().get().getName());
	}

	/**
	 * Ensure all test methods skipped, except non-ignore.
	 */
	@AfterAll
	public static void verifyCorrectTestsRun() {
		assertTrue(testsRun.contains("shouldRun"), "Incorrect test run");
		assertEquals(1, testsRun.size(), "Incorrect number of tests run");
	}

}
