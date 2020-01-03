package net.officefloor.frame.impl.execute.thread;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure able to spawn a {@link ThreadState} with a {@link FlowCallback}.
 *
 * @author Daniel Sagenschneider
 */
public class SpawnThreadStateCallbackTest extends AbstractOfficeConstructTestCase {

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
		assertTrue("Should have invoked callback", work.isCallback);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private boolean isSpawnInvoked = false;

		private boolean isCallback = false;

		public void trigger(ReflectiveFlow flow) {
			assertFalse("Should not yet execute spawned thread state", this.isSpawnInvoked);
			flow.doFlow(null, (escalation) -> {
				assertTrue("Spawn should have been invoked before callback", this.isSpawnInvoked);
				assertFalse("Should only callback once", this.isCallback);
				this.isCallback = true;
			});
		}

		public void spawn() {
			this.isSpawnInvoked = true;
		}
	}

}