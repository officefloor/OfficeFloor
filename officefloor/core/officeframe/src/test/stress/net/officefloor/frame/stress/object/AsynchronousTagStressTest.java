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
public class AsynchronousTagStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(AsynchronousTagStressTest.class);
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

		// Construct the managed objects
		this.constructManagedObject("ASYNCHRONOUS_ONE", null, () -> new Asynchronous()).setTimeout(1000);
		this.constructManagedObject("ASYNCHRONOUS_TWO", null, () -> new Asynchronous()).setTimeout(1000);

		// Construct the functions
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder taskOne = this.constructFunction(work, "taskOne");
		context.loadResponsibleTeam(taskOne.getBuilder());
		taskOne.buildParameter();
		taskOne.buildObject("ASYNCHRONOUS_ONE", context.getManagedObjectScope());
		taskOne.buildFlow("taskTwo", Asynchronous.class, false);
		ReflectiveFunctionBuilder taskTwo = this.constructFunction(work, "taskTwo");
		context.loadOtherTeam(taskTwo.getBuilder());
		taskTwo.buildParameter();
		taskTwo.buildObject("ASYNCHRONOUS_TWO", context.getManagedObjectScope());
		taskTwo.buildFlow("taskOne", Asynchronous.class, false);

		// Run
		context.setInitialFunction("taskOne", null);
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {

		private final StressContext context;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void taskOne(Asynchronous parameter, Asynchronous managedObject, ReflectiveFlow taskTwo) {

			// Notify complete for other task
			if (parameter != null) {
				parameter.listener.complete(null);
			}

			// Determine if continue
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Trigger asynchronous operation
			managedObject.listener.start(null);

			// Call other task to complete operation
			taskTwo.doFlow(managedObject, null);
		}

		public void taskTwo(Asynchronous parameter, Asynchronous managedObject, ReflectiveFlow taskOne) {
			this.taskOne(parameter, managedObject, taskOne);
		}
	}

	/**
	 * {@link AsynchronousManagedObject}.
	 */
	private static class Asynchronous implements AsynchronousManagedObject {

		private AsynchronousContext listener;

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