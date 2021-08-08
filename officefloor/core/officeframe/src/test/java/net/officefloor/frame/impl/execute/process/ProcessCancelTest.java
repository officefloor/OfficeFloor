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

package net.officefloor.frame.impl.execute.process;

import net.officefloor.frame.api.escalate.ProcessCancelledEscalation;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests cancelling a {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessCancelTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure cancel {@link ProcessState}.
	 */
	public void testCancelProcess() throws Exception {

		// Obtain the office name
		String officeName = this.getOfficeName();

		// Create team to allow blocking (and cancel process)
		this.constructTeam("TEAM", OnePersonTeamSource.class);

		// Build the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder block = this.constructFunction(work, "block");
		block.setNextFunction("next");
		block.getBuilder().setResponsibleTeam("TEAM");
		this.constructFunction(work, "next");

		// Obtain the function
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();
		FunctionManager function = officeFloor.getOffice(officeName).getFunctionManager("block");

		// Ensure invoke next function (without cancel)
		work.reset();
		work.isContinue = true;
		WaitFlowCallback notCancelled = new WaitFlowCallback();
		function.invokeProcess(null, notCancelled);
		assertNull("Should not fail on run through", notCancelled.waitForCompletion());
		assertTrue("Should invoke next function", work.isNextInvoked);

		// Ensure now cancel, so no next invoked
		work.reset();
		WaitFlowCallback cancelled = new WaitFlowCallback();
		work.processManager = function.invokeProcess(null, cancelled);
		work.isContinue = true;
		Throwable failure = cancelled.waitForCompletion();
		assertFalse("Should not invoke the next function: " + failure, work.isNextInvoked);
		assertNotNull("Should have failure, process cancelled", failure);
		assertTrue("Incorrect failure: " + failure.getMessage() + " [" + failure.getClass().getName() + "]",
				failure instanceof ProcessCancelledEscalation);
	}

	public class TestWork {

		private volatile ProcessManager processManager;

		private volatile boolean isContinue = false;

		private volatile boolean isNextInvoked = false;

		private void reset() {
			this.processManager = null;
			this.isContinue = false;
			this.isNextInvoked = false;
		}

		public void block() throws Exception {
			ProcessCancelTest.this.waitForTrue(() -> this.isContinue);

			// Cancel (done here to ensure before next)
			// (can not do on test thread, as blocks waiting access to thread)
			if (this.processManager != null) {
				this.processManager.cancel();
			}
		}

		public void next() {
			this.isNextInvoked = true;
		}
	}

	private class WaitFlowCallback implements FlowCallback {

		private boolean isComplete = false;

		private Throwable failure = null;

		private synchronized Throwable waitForCompletion() throws Exception {
			long startTime = System.currentTimeMillis();
			while (!isComplete) {
				ProcessCancelTest.this.timeout(startTime);
				this.wait(10);
			}
			return this.failure;
		}

		/*
		 * ================ FlowCallback =================
		 */

		@Override
		public synchronized void run(Throwable escalation) throws Throwable {
			this.failure = escalation;
			this.isComplete = true;
			this.notifyAll();
		}
	}

}
