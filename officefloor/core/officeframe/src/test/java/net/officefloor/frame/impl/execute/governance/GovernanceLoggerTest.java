package net.officefloor.frame.impl.execute.governance;

import java.util.logging.Logger;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveGovernanceBuilder;

/**
 * Ensure able to obtain {@link Logger} for {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceLoggerTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure appropriate {@link Logger}.
	 */
	public void testLogger() throws Exception {

		// Create the task
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.getBuilder().addGovernance("GOVERNANCE");

		// Provide governance
		final String GOVERNANCE_NAME = "GOVERNANCE";
		TestGovernance govern = new TestGovernance();
		ReflectiveGovernanceBuilder governance = this.constructGovernance(govern, GOVERNANCE_NAME);
		governance.enforce("enforce").buildGovernanceContext();

		// Invoke function confirming logger
		this.invokeFunction("task", null);

		// Ensure correct logger
		assertNotNull("Should have govenrance logger", govern.logger);
		assertEquals("Incorrect logger", GOVERNANCE_NAME, govern.logger.getName());
	}

	/**
	 * Test {@link Governance}.
	 */
	public class TestGovernance {

		private Logger logger;

		public void enforce(Object[] extensions, GovernanceContext<None> context) {
			this.logger = context.getLogger();
		}
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void task() {
			// nothing as confirming logging of governance
		}
	}

}