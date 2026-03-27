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

package net.officefloor.frame.impl.execute.managedobject.input;

import java.util.function.BiConsumer;

import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.BackPressureTeamSource;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensures appropriately handles the back pressure.
 *
 * @author Daniel Sagenschneider
 */
public class BackPressureTeamManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure handles immediate failure.
	 */
	public void testBackPressure() throws Exception {
		this.doBackPressureTest(0, (work, escalation) -> {
			assertSame("Should propagate the escalation", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION, escalation);
		});
	}

	/**
	 * Ensure handle {@link FlowCallback} propagating failure.
	 */
	public void testFlowBackPressure() throws Exception {
		this.doBackPressureTest(1, (work, escalation) -> {
			assertSame("Should propagate via function", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION, work.failure);
			assertSame("Should propagate the escalation", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION, escalation);
		});
	}

	/**
	 * Ensure handle next {@link ManagedFunction} propagating failure.
	 */
	public void testNextBackPressure() throws Exception {
		this.doBackPressureTest(2, (work, escalation) -> {
			assertSame("Should propagate the escalation", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION, escalation);
		});
	}

	/**
	 * Undertakes the back pressure test.
	 * 
	 * @param flowIndex Index of {@link ManagedObjectExecuteContext} to invoke.
	 * @param validator Validates the result.
	 */
	private void doBackPressureTest(int flowIndex, BiConsumer<TestWork, Throwable> validator) throws Exception {

		// Construct the object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (context) -> {
			context.addFlow(null);
			context.addFlow(null);
			context.addFlow(null);
		};
		ManagingOfficeBuilder<?> managingOffice = object.managedObjectBuilder.setManagingOffice(this.getOfficeName());
		managingOffice.setInputManagedObjectName("MO");
		managingOffice.linkFlow(0, "backPressure");
		managingOffice.linkFlow(1, "flow");
		managingOffice.linkFlow(2, "next");

		// Construct the functions
		TestWork work = new TestWork();

		// Flow
		ReflectiveFunctionBuilder flow = this.constructFunction(work, "flow");
		flow.buildFlow("backPressure", null, false);

		// Next
		this.constructFunction(work, "next").setNextFunction("backPressure");

		// Function causing back pressure by team
		this.constructTeam("BACK_PRESSURE", BackPressureTeamSource.class);
		this.constructFunction(work, "backPressure").getBuilder().setResponsibleTeam("BACK_PRESSURE");

		// Open OfficeFloor
		try (OfficeFloor officeFloor = this.constructOfficeFloor()) {
			officeFloor.openOfficeFloor();

			// Undertake flow
			Closure<Throwable> propagatedFailure = new Closure<>();
			object.managedObjectServiceContext.invokeProcess(flowIndex, null, object, 0, (escalation) -> {
				propagatedFailure.value = escalation;
			});

			// Ensure handle escalation
			validator.accept(work, propagatedFailure.value);
		}
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {

		private volatile Throwable failure = null;

		public void flow(ReflectiveFlow flow) {
			flow.doFlow(null, (escalation) -> {
				this.failure = escalation;
				if (escalation != null) {
					throw escalation;
				}
			});
		}

		public void next() {
			// ensure next also propagates the back pressure
		}

		public void backPressure() throws Exception {
			fail("Back pressure function should not be invoked");
		}
	}

}
