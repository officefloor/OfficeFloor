package net.officefloor.frame.test;

import static org.junit.Assume.assumeFalse;

/**
 * JUnit 4 skip logic.
 * 
 * @author Daniel Sagenschneider
 */
public class SkipJUnit4 extends SkipUtil {

	/**
	 * Invoke within test to skip if Stress test.
	 */
	public static void skipStress() {
		assumeFalse(isSkipStressTests());
	}

	/**
	 * Invoke within test to skip if Docker not available.
	 */
	public static void skipDocker() {
		assumeFalse(isSkipTestsUsingDocker());
	}

	/**
	 * Invoke within test to skip if GCloud not available.
	 */
	public static void skipGCloud() {
		assumeFalse(isSkipTestsUsingGCloud());
	}

	/**
	 * All access via static methods.
	 */
	private SkipJUnit4() {
		// All access via static methods
	}
}