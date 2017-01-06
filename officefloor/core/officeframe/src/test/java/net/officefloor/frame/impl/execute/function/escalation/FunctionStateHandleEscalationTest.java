/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.function.escalation;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensures the {@link FunctionState} can handle its own {@link Escalation}.
 *
 * @author Daniel Sagenschneider
 */
public class FunctionStateHandleEscalationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Flag to record task method invocations.
	 */
	public FunctionStateHandleEscalationTest() {
		this.setRecordReflectiveFunctionMethodsInvoked(true);
	}

	/**
	 * Ensures handles escalation by same {@link ManagedFunction}.
	 */
	public void test_Escalation_HandledBy_SameFunction() throws Exception {

		// Create the work object
		EscalationWorkObject workObject = new EscalationWorkObject(new Exception("test"));

		// Construct the office
		ReflectiveFunctionBuilder causeEscalation = this.constructFunction(workObject, "causeEscalation");
		causeEscalation.setNextFunction("nextTask"); // not invoked
		causeEscalation.getBuilder().addEscalation(workObject.failure.getClass(), "handleEscalation");
		this.constructFunction(workObject, "nextTask");
		this.constructFunction(workObject, "handleEscalation").buildParameter();

		// Invoke the function
		this.invokeFunction("causeEscalation", null);

		// Validate appropriate methods called
		this.validateReflectiveMethodOrder("causeEscalation", "handleEscalation");

		// Validate failure handled
		assertEquals("Incorrect exception", workObject.failure, workObject.handledFailure);
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

		public void causeEscalation() throws Throwable {
			throw failure;
		}

		public void nextTask() {
		}

		public void handleEscalation(Throwable handledfailure) {
			this.handledFailure = handledfailure;
		}
	}

}