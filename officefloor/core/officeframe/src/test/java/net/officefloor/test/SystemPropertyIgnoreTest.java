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
			SkipUtil.GCLOUD_AVAILABLE_SYSTEM_PROPERTY, "false");

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

	/**
	 * Ensure all test methods skipped, except non-ignore.
	 */
	@AfterAll
	public static void verifyCorrectTestsRun() {
		assertTrue(testsRun.contains("shouldRun"), "Incorrect test run");
		assertEquals(1, testsRun.size(), "Incorrect number of tests run");
	}

}