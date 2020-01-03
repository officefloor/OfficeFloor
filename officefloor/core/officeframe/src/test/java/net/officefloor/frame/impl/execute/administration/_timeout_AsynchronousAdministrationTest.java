package net.officefloor.frame.impl.execute.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.escalate.AsynchronousFlowTimedOutEscalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveAdministrationBuilder;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure can use {@link AsynchronousFlow} for {@link Administration}.
 *
 * @author Daniel Sagenschneider
 */
public class _timeout_AsynchronousAdministrationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure undertakes {@link Administration} with {@link AsynchronousFlow}.
	 */
	public void testAsynchronousAdministration() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		this.constructFunction(work, "trigger").setNextFunction("task");
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");

		// Construct the administration
		ReflectiveAdministrationBuilder admin = task.preAdminister("preTask");
		admin.buildAsynchronousFlow();
		admin.getBuilder().setAsynchronousFlowTimeout(10);

		// Trigger the flow
		Closure<Throwable> escalation = new Closure<Throwable>();
		Office office = this.triggerFunction("trigger", null, (error) -> escalation.value = error);
		assertNull("Should be no failure: " + escalation.value, escalation.value);
		assertFalse("Should not invoke task", work.isTaskInvoked);

		// Time out administration
		this.adjustCurrentTimeMillis(100);
		office.runAssetChecks();

		// Ensure timed out
		assertTrue("Should timeout asynchronous flow: " + escalation.value,
				escalation.value instanceof AsynchronousFlowTimedOutEscalation);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private boolean isTaskInvoked = false;

		public void trigger() {
			// testing
		}

		public void preTask(Object[] extensions, AsynchronousFlow flow) {
			// testing
		}

		public void task() {
			this.isTaskInvoked = true;
		}
	}

}