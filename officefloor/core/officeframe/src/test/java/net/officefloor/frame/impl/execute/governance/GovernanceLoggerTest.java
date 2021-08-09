/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
