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
package net.officefloor.frame.impl.execute.managedobject.input;

import java.util.function.BiConsumer;

import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure {@link EscalationHandler} instances can propagate failure to handle
 * {@link Escalation}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropagateObjectEscalationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Failure to propagate.
	 */
	private final RuntimeException propagateFailure = new RuntimeException("TEST PROPAGATE");

	/**
	 * Ensure handles immediate failure.
	 */
	public void testImmediateFailure() throws Exception {
		this.doPropagateEscalationTest(0, (work, escalation) -> {
			assertSame("Should propagate the escalation", this.propagateFailure, escalation);
		});
	}

	/**
	 * Ensure handle {@link ManagedFunction} propagating failrue.
	 */
	public void testFunctionPropagateFailure() throws Exception {
		this.doPropagateEscalationTest(1, (work, escalation) -> {
			assertSame("Should propagate via function", this.propagateFailure, work.flowFailure);
			assertSame("Should propagate the escalation", this.propagateFailure, escalation);
		});
	}

	/**
	 * Ensure propagate {@link Escalation} to invoke process.
	 * 
	 * @param flowIndex Index of {@link ManagedObjectExecuteContext} to invoke.
	 * @param validator Validates the result.
	 */
	private void doPropagateEscalationTest(int flowIndex, BiConsumer<TestWork, Throwable> validator) throws Exception {

		// Construct the object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (context) -> {
			context.addFlow(null);
			context.addFlow(null);
		};
		ManagingOfficeBuilder<?> managingOffice = object.managedObjectBuilder.setManagingOffice(this.getOfficeName());
		managingOffice.setInputManagedObjectName("MO");
		managingOffice.linkFlow(0, "failure");
		managingOffice.linkFlow(1, "flow");

		// Construct the functions
		TestWork work = new TestWork();

		// Flow -> failure
		ReflectiveFunctionBuilder flow = this.constructFunction(work, "flow");
		flow.buildFlow("failure", null, false);

		// Failure
		this.constructFunction(work, "failure");

		// Open OfficeFloor
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();
		try {

			// Undertake flow
			Closure<Throwable> propagatedFailure = new Closure<>();
			object.managedObjectExecuteContext.invokeProcess(flowIndex, null, object, 0, (escalation) -> {
				propagatedFailure.value = escalation;
			});

			// Ensure handle escalation
			validator.accept(work, propagatedFailure.value);

		} finally {
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private volatile Throwable flowFailure = null;

		public void flow(ReflectiveFlow flow) {
			flow.doFlow(null, (escalation) -> {
				this.flowFailure = escalation;
				throw escalation;
			});
		}

		public void failure() throws Throwable {
			throw PropagateObjectEscalationTest.this.propagateFailure;
		}
	}

}