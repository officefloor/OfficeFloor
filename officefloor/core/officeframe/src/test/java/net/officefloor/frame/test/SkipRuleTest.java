package net.officefloor.frame.test;

import org.junit.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Tests the {@link SkipRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class SkipRuleTest extends OfficeFrameTestCase {

	/**
	 * Ensure skips running test.
	 */
	public void testSkip() throws Throwable {
		try {
			this.evaluateSkipRule(new SkipRule(true), null);
			fail("Should not be successful");
		} catch (AssumptionViolatedException ex) {
			assertEquals("Incorrect skip reason", "NOT RUNNING TEST", ex.getMessage());
		}
	}

	/**
	 * Ensure provides details with {@link Description}.
	 */
	public void testSkipWithDescription() throws Throwable {
		try {
			this.evaluateSkipRule(new SkipRule(true, "TEST SKIP"),
					Description.createTestDescription(SkipRuleTest.class, "testMethod"));
			fail("Should not be successful");
		} catch (AssumptionViolatedException ex) {
			assertEquals("Incorrect skip reason",
					"NOT RUNNING TEST " + SkipRuleTest.class.getName() + ".testMethod(): TEST SKIP", ex.getMessage());
		}
	}

	/**
	 * Ensure not skip test.
	 */
	public void testNotSkip() throws Throwable {
		assertTrue("Should run test", this.evaluateSkipRule(new SkipRule(false, "Not skip"), null));
	}

	/**
	 * Evaluates the {@link SkipRule} indicating whether test was run.
	 * 
	 * @param rule        {@link SkipRule} to use.
	 * @param description {@link Description}.
	 * @return <code>true</code> if test run.
	 */
	private boolean evaluateSkipRule(SkipRule rule, Description description) throws Throwable {
		Closure<Boolean> isRun = new Closure<>(false);
		rule.apply(new Statement() {

			@Override
			public void evaluate() throws Throwable {
				isRun.value = true;
			}
		}, description).evaluate();
		return isRun.value;
	}

}