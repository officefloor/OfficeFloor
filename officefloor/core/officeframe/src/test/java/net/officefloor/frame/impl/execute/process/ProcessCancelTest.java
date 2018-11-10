/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.process;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.ProcessCancelledException;
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
		work.isContinue = true;
		work.isNextInvoked = false;
		WaitFlowCallback notCancelled = new WaitFlowCallback();
		function.invokeProcess(null, notCancelled);
		assertNull("Should not fail on run through", notCancelled.waitForCompletion());
		assertTrue("Should invoke next function", work.isNextInvoked);

		// Ensure now cancel, so no next invoked
		work.isContinue = false;
		work.isNextInvoked = false;
		WaitFlowCallback cancelled = new WaitFlowCallback();
		ProcessManager manager = function.invokeProcess(null, cancelled);
		manager.cancel();
		work.isContinue = true;
		Throwable failure = cancelled.waitForCompletion();
		assertNotNull("Should have failure, process cancelled", failure);
		assertTrue("Incorrect failure: " + failure.getMessage() + " [" + failure.getClass().getName() + "]",
				failure instanceof ProcessCancelledException);
		assertFalse("Should not invoke the next function", work.isNextInvoked);
	}

	public class TestWork {

		private volatile boolean isContinue = false;

		private volatile boolean isNextInvoked = false;

		public void block() {
			ProcessCancelTest.this.waitForTrue(() -> this.isContinue);
		}

		public void next() {
			this.isNextInvoked = true;
		}
	}

	private static class WaitFlowCallback implements FlowCallback {

		private boolean isComplete = false;

		private Throwable failure = null;

		private synchronized Throwable waitForCompletion() throws Exception {
			while (!isComplete) {
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