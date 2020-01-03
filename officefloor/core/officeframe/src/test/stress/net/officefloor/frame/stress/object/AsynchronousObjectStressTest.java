package net.officefloor.frame.stress.object;

import junit.framework.TestSuite;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests the {@link AsynchronousManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousObjectStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(AsynchronousObjectStressTest.class);
	}

	@Override
	protected boolean isTestEachManagedObjectScope() {
		return true;
	}

	@Override
	protected int getIterationCount() {
		return 100000;
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Construct the managed object
		this.constructManagedObject("ASYNCHRONOUS", null, () -> new Asynchronous()).setTimeout(1000);

		// Construct the functions
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		context.loadResponsibleTeam(task.getBuilder());
		task.buildObject("ASYNCHRONOUS", context.getManagedObjectScope());
		task.buildFlow("spawn", Asynchronous.class, true);
		task.buildFlow("task", null, false);
		ReflectiveFunctionBuilder spawn = this.constructFunction(work, "spawn");
		context.loadOtherTeam(spawn.getBuilder());
		spawn.buildParameter();

		// Run
		context.setInitialFunction("task", null);
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {

		private final StressContext context;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void task(Asynchronous asynchronous, ReflectiveFlow spawn, ReflectiveFlow repeat) {

			// Ensure not within asynchronous operation
			assertFalse("No asynchronous operation for function", asynchronous.isWithinAsynchronousOperation);

			// Determine if continue
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Trigger asynchronous operation
			asynchronous.isWithinAsynchronousOperation = true;
			asynchronous.listener.start(null);

			// Spawn thread state to complete operation
			spawn.doFlow(asynchronous, (escalation) -> {
				assertNull("Should be now failure", escalation);
				assertFalse("No asynchronous operation for callback", asynchronous.isWithinAsynchronousOperation);

				// Repeat
				repeat.doFlow(null, null);
			});
		}

		public void spawn(Asynchronous asynchronous) {
			// Notify asynchronous complete
			asynchronous.isWithinAsynchronousOperation = false;
			asynchronous.listener.complete(null);
		}
	}

	/**
	 * {@link AsynchronousManagedObject}.
	 */
	private static class Asynchronous implements AsynchronousManagedObject {

		private AsynchronousContext listener;

		private volatile boolean isWithinAsynchronousOperation = false;

		@Override
		public void setAsynchronousContext(AsynchronousContext listener) {
			this.listener = listener;
		}

		@Override
		public Object getObject() {
			return this;
		}
	}

}