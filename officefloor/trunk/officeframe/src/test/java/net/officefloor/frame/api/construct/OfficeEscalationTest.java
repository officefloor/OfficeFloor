/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.api.construct;

import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Tests handling escalations.
 * 
 * @author Daniel
 */
public class OfficeEscalationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures handles escalation with reset of {@link ThreadState}.
	 */
	public void testHandleEscalationWithReset() throws Exception {
		this.doInvokeWorkTest(true, "first", "handleEscalation");
	}

	/**
	 * Ensures handles escalation without reseting {@link ThreadState}.
	 */
	public void testHandleEscalationWithoutReset() throws Exception {
		this.doInvokeWorkTest(false, "first", "handleEscalation");
	}

	/**
	 * Sets up the office and invokes the work on it.
	 * 
	 * @param isResetThreadState
	 *            Indicates if should reset {@link ThreadState} on escalation.
	 * @param expectedInvokedMethods
	 *            The expected methods to be invoked.
	 */
	private void doInvokeWorkTest(boolean isResetThreadState,
			String... expectedInvokedMethods) throws Exception {

		// Create the work object
		EscalationWorkObject workObject = new EscalationWorkObject(
				new Exception("test"));

		// Construct the office
		this.constructTeam("team", new PassiveTeam());
		ReflectiveWorkBuilder workBuilder = this.constructWork(workObject,
				"work", "first");
		ReflectiveTaskBuilder firstTaskBuilder = workBuilder.buildTask("first",
				"team");
		firstTaskBuilder.getBuilder().setNextTaskInFlow("second");
		firstTaskBuilder.getBuilder().addEscalation(
				workObject.failure.getClass(), isResetThreadState,
				"handleEscalation");
		workBuilder.buildTask("second", "team");
		ReflectiveTaskBuilder escalationTaskBuilder = workBuilder.buildTask(
				"handleEscalation", "team");
		escalationTaskBuilder.buildParameter();

		// Invoke the work
		this.invokeWork("office", "work", null);

		// Validate failure handled
		assertEquals("Incorrect exception", workObject.failure,
				workObject.handledFailure);

		// Validate appropriate methods called
		this.validateReflectiveMethodOrder(expectedInvokedMethods);
	}

	/**
	 * Work object with methods.
	 */
	public static class EscalationWorkObject {

		public final Throwable failure;

		public Throwable handledFailure = null;

		public EscalationWorkObject(Throwable failure) {
			this.failure = failure;
		}

		public void first() throws Throwable {
			throw failure;
		}

		public void second() {
		}

		public void handleEscalation(Throwable handledfailure) {
			this.handledFailure = handledfailure;
		}
	}
}
