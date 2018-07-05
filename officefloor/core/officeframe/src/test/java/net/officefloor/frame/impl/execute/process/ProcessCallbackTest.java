/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.process;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensure {@link FlowCallback} invoked on completion of {@link ProcessState}.
 *
 * @author Daniel Sagenschneider
 */
public class ProcessCallbackTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link FlowCallback} invoked on completion of
	 * {@link ProcessState}.
	 */
	public void testProcessCallback() throws Exception {
		TestWork work = new TestWork();
		this.constructFunction(work, "task");
		String officeName = this.getOfficeName();
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();
		FunctionManager function = officeFloor.getOffice(officeName).getFunctionManager("task");
		boolean[] isCompletionInvoked = new boolean[] { false };
		function.invokeProcess(null, (escalation) -> isCompletionInvoked[0] = true);
		assertTrue("Task should be invoked", work.isTaskInvoked);
		assertTrue("Callback should be invoked", isCompletionInvoked[0]);
	}

	/**
	 * Ensure able to invoke {@link ProcessState} without {@link FlowCallback}.
	 */
	public void testNoProcessCallabck() throws Exception {
		TestWork work = new TestWork();
		this.constructFunction(work, "task");
		String officeName = this.getOfficeName();
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();
		FunctionManager function = officeFloor.getOffice(officeName).getFunctionManager("task");
		function.invokeProcess(null, null);
		assertTrue("Task should be invoked", work.isTaskInvoked);
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {

		public boolean isTaskInvoked = false;

		public void task() {
			this.isTaskInvoked = true;
		}
	}

}