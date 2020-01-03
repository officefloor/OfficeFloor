package net.officefloor.frame.impl.execute.function.asynchronous;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure ignores the second {@link AsynchronousFlow} completion.
 * 
 * @author Daniel Sagenschneider
 */
public class IgnoreSecondAsynchronousFlowCompletionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure ignores the second {@link AsynchronousFlow} completion.
	 */
	public void testAsynchronousFlow() throws Exception {

		// Construct the object
		TestObject object = new TestObject();
		this.constructManagedObject(object, "MO", this.getOfficeName());

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildAsynchronousFlow();
		trigger.buildObject("MO", ManagedObjectScope.THREAD);

		// Ensure only uses first completion
		this.invokeFunction("triggerAsynchronousFlow", null);
		assertEquals("Should only run the first completion", "first only", object.value);
	}

	public class TestObject {
		private String value;
	}

	public class TestWork {

		public void triggerAsynchronousFlow(AsynchronousFlow flow, TestObject object) {

			// Only first completion used
			flow.complete(() -> object.value = "first only");

			// Remaining completions are ignored
			for (int i = 2; i < 10; i++) {
				final int index = i;
				flow.complete(() -> object.value = "Not use " + index);
			}
		}
	}

}