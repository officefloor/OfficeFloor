package net.officefloor.frame.stress;

import junit.framework.TestSuite;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Simple test to validate the {@link AbstractStressTestCase}.
 *
 * @author Daniel Sagenschneider
 */
public class ValidateStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(ValidateStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Create the function
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildParameter();
		task.buildFlow("flow", null, false);
		context.loadOtherTeam(task.getBuilder());
		ReflectiveFunctionBuilder flow = this.constructFunction(work, "flow");
		context.loadResponsibleTeam(flow.getBuilder());

		// Indicate start point
		context.setInitialFunction("task", context.getMaximumIterations());
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private StressContext context;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void task(Integer iterations, ReflectiveFlow flow) {
			for (int i = 0; i < iterations; i++) {
				flow.doFlow(null, null);
			}
		}

		public void flow() {
			this.context.incrementIteration();
		}
	}

}