/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

import net.officefloor.test.system.EnvironmentExtension;

/**
 * Ensure skip tests based on environment variables.
 * 
 * @author Daniel Sagenschneider
 */
public class EnvironmentIgnoreTest {

	private static List<String> testsRun = new LinkedList<>();

	@RegisterExtension
	public static final EnvironmentExtension env = new EnvironmentExtension(SkipUtil.SKIP_STRESS_ENVIRONMENT_VARIABLE,
			"true", SkipUtil.DOCKER_AVAILABLE_ENVIRONMENT_VARIABLE, "false",
			SkipUtil.GCLOUD_AVAILABLE_ENVIRONMENT_VARIABLE, "false", SkipUtil.AWS_AVAILABLE_ENVIRONMENT_VARIABLE,
			"false");

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
