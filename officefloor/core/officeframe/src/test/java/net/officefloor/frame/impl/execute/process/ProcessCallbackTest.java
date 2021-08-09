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
