package net.officefloor.frame.stress.function;

import junit.framework.TestSuite;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests running the same {@link ManagedFunction} many times.
 * 
 * @author Daniel Sagenschneider
 */
public class RepeatFunctionStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(RepeatFunctionStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Construct functions
		RepeatTask repeat = new RepeatTask(context);
		ReflectiveFunctionBuilder function = this.constructFunction(repeat, "repeat");
		context.loadResponsibleTeam(function.getBuilder());
		function.buildFlow("repeat", null, false);

		// Run the repeats
		context.setInitialFunction("repeat", null);
	}

	/**
	 * Test functionality.
	 */
	public class RepeatTask {

		private final StressContext context;

		public RepeatTask(StressContext context) {
			this.context = context;
		}

		public void repeat(ReflectiveFlow repeat) {

			// Determine if complete
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Repeat
			repeat.doFlow(null, null);
		}
	}

}