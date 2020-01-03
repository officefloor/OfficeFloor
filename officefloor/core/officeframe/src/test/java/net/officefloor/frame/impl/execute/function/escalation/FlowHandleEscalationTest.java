/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.function.escalation;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests handling escalations by {@link FlowCallback} and {@link Flow}
 * {@link EscalationHandler} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowHandleEscalationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Flag to record task method invocations.
	 */
	public FlowHandleEscalationTest() {
		this.setRecordReflectiveFunctionMethodsInvoked(true);
	}

	/**
	 * <p>
	 * Ensures a parallel owner {@link FunctionState} has opportunity to handle
	 * the escalation via a {@link FlowCallback}.
	 * <p>
	 * This is represents a method call allowing the caller to handle exceptions
	 * from the invoked method.
	 */
	public void test_Escalation_HandledBy_InvokingParallelOwner() throws Exception {

		// Create the work object
		EscalationWorkObject workObject = new EscalationWorkObject(new Exception("test"));

		// Construct the office
		ReflectiveFunctionBuilder parallelOwner = this.constructFunction(workObject, "parallelOwner");
		parallelOwner.buildFlow("causeEscalation", null, false);
		parallelOwner.setNextFunction("nextTask");
		parallelOwner.getBuilder().addEscalation(workObject.failure.getClass(), "handleEscalation");
		this.constructFunction(workObject, "causeEscalation");
		this.constructFunction(workObject, "nextTask");
		this.constructFunction(workObject, "handleEscalation").buildParameter();

		// Invoke the function
		this.invokeFunction("parallelOwner", null);

		// Validate appropriate methods called
		this.validateReflectiveMethodOrder("parallelOwner", "causeEscalation", "handleEscalation");

		// Validate failure handled
		assertEquals("Incorrect exception", workObject.failure, workObject.handledFailure);
	}

	/**
	 * <p>
	 * Ensures a parallel owner {@link FunctionState} has opportunity to handle
	 * the escalation via a {@link FlowCallback} from any {@link FunctionState}
	 * of the {@link Flow}.
	 * <p>
	 * This is represents a method call allowing the caller to handle exceptions
	 * from the invoked method.
	 */
	public void test_Escalation_HandledBy_ParallelOwner() throws Exception {

		// Create the work object
		EscalationWorkObject workObject = new EscalationWorkObject(new Exception("test"));

		// Construct the office
		ReflectiveFunctionBuilder parallelOwner = this.constructFunction(workObject, "parallelOwner");
		parallelOwner.buildFlow("beforeEscalation", null, false);
		parallelOwner.setNextFunction("nextTask");
		parallelOwner.getBuilder().addEscalation(workObject.failure.getClass(), "handleEscalation");
		ReflectiveFunctionBuilder beforeEscalation = this.constructFunction(workObject, "beforeEscalation");
		beforeEscalation.setNextFunction("causeEscalation");
		this.constructFunction(workObject, "causeEscalation");
		this.constructFunction(workObject, "nextTask");
		this.constructFunction(workObject, "handleEscalation").buildParameter();

		// Invoke the function
		this.invokeFunction("parallelOwner", null);

		// Validate appropriate methods called
		this.validateReflectiveMethodOrder("parallelOwner", "beforeEscalation", "causeEscalation", "handleEscalation");

		// Validate failure handled
		assertEquals("Incorrect exception", workObject.failure, workObject.handledFailure);
	}

	/**
	 * <p>
	 * Ensures a parallel ancestor {@link FunctionState} has opportunity to
	 * handle the escalation via a {@link FlowCallback}.
	 * <p>
	 * This is represents a method call allowing the caller to handle exceptions
	 * from the invoked method.
	 */
	public void test_Escalation_HandledBy_InvokingParallelAncestor() throws Exception {

		// Create the work object
		EscalationWorkObject workObject = new EscalationWorkObject(new Exception("test"));

		// Construct the office
		ReflectiveFunctionBuilder parallelAncestor = this.constructFunction(workObject, "parallelAncestor");
		parallelAncestor.buildFlow("parallelOwner", null, false);
		parallelAncestor.setNextFunction("nextTask");
		parallelAncestor.getBuilder().addEscalation(workObject.failure.getClass(), "handleEscalation");
		ReflectiveFunctionBuilder parallelOwner = this.constructFunction(workObject, "parallelOwner");
		parallelOwner.buildFlow("causeEscalation", null, false);
		parallelOwner.setNextFunction("nextTask");
		this.constructFunction(workObject, "causeEscalation");
		this.constructFunction(workObject, "nextTask");
		this.constructFunction(workObject, "handleEscalation").buildParameter();

		// Invoke the function
		this.invokeFunction("parallelAncestor", null);

		// Validate appropriate methods called
		this.validateReflectiveMethodOrder("parallelAncestor", "parallelOwner", "causeEscalation", "handleEscalation");

		// Validate failure handled
		assertEquals("Incorrect exception", workObject.failure, workObject.handledFailure);
	}

	/**
	 * <p>
	 * Ensures a parallel ancestor {@link FunctionState} has opportunity to
	 * handle the escalation via a {@link FlowCallback} from any
	 * {@link FunctionState} of the {@link Flow}.
	 * <p>
	 * This is represents a method call allowing the caller to handle exceptions
	 * from the invoked method.
	 */
	public void test_Escalation_HandledBy_ParallelAncestor() throws Exception {

		// Create the work object
		EscalationWorkObject workObject = new EscalationWorkObject(new Exception("test"));

		// Construct the office
		ReflectiveFunctionBuilder parallelAncestor = this.constructFunction(workObject, "parallelAncestor");
		parallelAncestor.buildFlow("beforeParallelOwner", null, false);
		parallelAncestor.setNextFunction("nextTask");
		parallelAncestor.getBuilder().addEscalation(workObject.failure.getClass(), "handleEscalation");
		ReflectiveFunctionBuilder beforeParallelOwner = this.constructFunction(workObject, "beforeParallelOwner");
		beforeParallelOwner.setNextFunction("parallelOwner");
		ReflectiveFunctionBuilder parallelOwner = this.constructFunction(workObject, "parallelOwner");
		parallelOwner.buildFlow("causeEscalation", null, false);
		parallelOwner.setNextFunction("nextTask");
		this.constructFunction(workObject, "causeEscalation");
		this.constructFunction(workObject, "nextTask");
		this.constructFunction(workObject, "handleEscalation").buildParameter();

		// Invoke the function
		this.invokeFunction("parallelAncestor", null);

		// Validate appropriate methods called
		this.validateReflectiveMethodOrder("parallelAncestor", "beforeParallelOwner", "parallelOwner",
				"causeEscalation", "handleEscalation");

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

		public void parallelAncestor(ReflectiveFlow flow) {
			flow.doFlow(null, FlowCallback.ESCALATE);
		}

		public void beforeParallelOwner() {
		}

		public void parallelOwner(ReflectiveFlow flow) {
			flow.doFlow(null, FlowCallback.ESCALATE);
		}

		public void beforeEscalation() {
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
