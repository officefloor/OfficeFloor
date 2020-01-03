package net.officefloor.frame.impl.execute.thread;

import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure able to spawn a {@link ThreadState}.
 *
 * @author Daniel Sagenschneider
 */
public class SpawnThreadStateTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can spawn a {@link ThreadState}.
	 */
	public void testSpawnThreadState() throws Exception {

		// Construct functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "trigger");
		trigger.buildFlow("spawn", null, true);
		this.constructFunction(work, "spawn");

		// Ensure spawn thread state
		this.invokeFunctionAndValidate("trigger", null, "trigger", "spawn");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void trigger(ReflectiveFlow flow) {
			flow.doFlow(null, null);
		}

		public void spawn() {
		}
	}

}