package net.officefloor.frame.impl.execute.function.escalation;

import org.junit.Assert;

import junit.framework.TestCase;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensure {@link Escalation} from {@link Assert} propagated back to
 * {@link TestCase}.
 *
 * @author Daniel Sagenschneider
 */
public class AssertEscalationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link Assert} failure is propagated to {@link TestCase}.
	 */
	public void test_Assertion_PropagatedTo_TestCase() throws Exception {

		// Construct function
		TestWork work = new TestWork();
		this.constructFunction(work, "task");

		// Ensure propagate failure
		try {
			this.invokeFunction("task", null);
			fail("Should not be successful");
		} catch (AssertionError ex) {
			assertEquals("Incorrect assertion failure", "TEST FAILURE", ex.getMessage());
		}

	}

	public class TestWork {
		public void task() {
			fail("TEST FAILURE");
		}
	}

}