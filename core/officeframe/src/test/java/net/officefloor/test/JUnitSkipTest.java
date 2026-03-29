package net.officefloor.test;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Ensure {@link JUnit5Skip} is able to skip test.
 * 
 * @author Daniel Sagenschneider
 */
public class JUnitSkipTest {

	/**
	 * Trigger skip.
	 */
	@Order(1)
	public @RegisterExtension AfterEachCallback skip = new AfterEachCallback() {

		@Override
		public void afterEach(ExtensionContext context) throws Exception {
			JUnit5Skip.skip(context, "Should skip", null);
		}
	};

	/**
	 * Failure on start up to skip.
	 */
	@Order(2)
	public @RegisterExtension BeforeEachCallback setupFailure = new BeforeEachCallback() {

		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			throw new Exception("Test skipping after failing test setup");
		}
	};

	/**
	 * Test failing.
	 */
	@Test
	public void test() {
		fail("Should not be invoked");
	}

}