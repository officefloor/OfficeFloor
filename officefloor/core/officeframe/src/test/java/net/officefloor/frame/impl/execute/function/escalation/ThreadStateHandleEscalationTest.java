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

package net.officefloor.frame.impl.execute.function.escalation;

import java.util.logging.Logger;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Validates that escalations of tasks is appropriately managed by the
 * {@link EscalationHandler} instances.
 *
 * @author Daniel Sagenschneider
 */
public class ThreadStateHandleEscalationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures handles escalation by the {@link Office}
	 * {@link EscalationProcedure}.
	 */
	public void test_Escalation_HandledBy_OfficeEscalationProcedure() throws Throwable {

		// Create the escalation
		RuntimeException escalation = new RuntimeException("Escalation");

		// Add office escalation to handle escalation
		this.getOfficeBuilder().addEscalation(RuntimeException.class, "officeEscalation");

		// Construct work
		EscalationHandlerWork work = new EscalationHandlerWork();
		this.constructFunction(work, "task").buildParameter();
		this.constructFunction(work, "officeEscalation").buildParameter();

		// Execute the task to have escalation handled
		this.invokeFunction("task", escalation);

		// Ensure escalation is handled by office escalation procedure
		assertEquals("Incorrect escalation", escalation, work.officeException);
	}

	/**
	 * Ensure failure of {@link Office} {@link EscalationProcedure} is handled
	 * by {@link OfficeFloor} {@link EscalationHandler}.
	 */
	public void test_OfficeEscalationProcedureFailure_HandledBy_OfficeFloorEscalation() throws Throwable {

		// Obtain the office name
		String officeName = this.getOfficeName();

		// Create the escalation
		RuntimeException escalation = new RuntimeException("Escalation");

		// Add office escalation to propagate escalation
		this.getOfficeBuilder().addEscalation(RuntimeException.class, "task");

		// Construct work
		EscalationHandlerWork work = new EscalationHandlerWork();
		this.constructFunction(work, "task").buildParameter();

		// Capture OfficeFloor escalation
		final Throwable[] failure = new Throwable[1];
		this.getOfficeFloorBuilder().setEscalationHandler(new EscalationHandler() {
			@Override
			public void handleEscalation(Throwable escalation) throws Throwable {
				failure[0] = escalation;
			}
		});

		// Must invoke directly via manager (to not provide callback)
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();
		FunctionManager function = officeFloor.getOffice(officeName).getFunctionManager("task");

		// Execute the task to have escalation handled
		function.invokeProcess(escalation, null);

		// Ensure escalation handled by OfficeFloor escalation handler
		assertSame("Incorrect escalation", escalation, failure[0]);
	}

	/**
	 * Ensures handles escalation by the {@link OfficeFloor}
	 * {@link EscalationHandler}.
	 */
	public void test_Escalation_HandledBy_OfficeFloorEscalation() throws Throwable {

		// Obtain the office name
		String officeName = this.getOfficeName();

		// Create the escalation
		RuntimeException escalation = new RuntimeException("Escalation");

		// Construct work
		EscalationHandlerWork work = new EscalationHandlerWork();
		this.constructFunction(work, "task").buildParameter();

		// Capture OfficeFloor escalation
		final Throwable[] failure = new Throwable[1];
		this.getOfficeFloorBuilder().setEscalationHandler(new EscalationHandler() {
			@Override
			public void handleEscalation(Throwable escalation) throws Throwable {
				failure[0] = escalation;
			}
		});

		// Must invoke directly via manager (to not provide callback)
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();
		FunctionManager function = officeFloor.getOffice(officeName).getFunctionManager("task");

		// Execute the task to have escalation handled
		function.invokeProcess(escalation, null);

		// Ensure escalation handled by OfficeFloor escalation handler
		assertSame("Incorrect escalation", escalation, failure[0]);
	}

	/**
	 * Ensure failure of {@link OfficeFloor} {@link EscalationHandler} is
	 * handled by {@link Logger}.
	 */
	public void test_OfficeFloorEscalationFailure_HandledBy_Logging() throws Throwable {

		// Obtain the office name
		String officeName = this.getOfficeName();

		// Create the escalation
		RuntimeException escalation = new RuntimeException("TEST ESCALATION");

		// Construct work
		EscalationHandlerWork work = new EscalationHandlerWork();
		this.constructFunction(work, "task").buildParameter();

		// Propagate OfficeFloor escalation
		this.getOfficeFloorBuilder().setEscalationHandler(new EscalationHandler() {
			@Override
			public void handleEscalation(Throwable escalation) throws Throwable {
				throw escalation;
			}
		});

		// Must invoke directly via manager (to not provide callback)
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();
		FunctionManager function = officeFloor.getOffice(officeName).getFunctionManager("task");

		// Execute the task to have escalation logged
		String log = this.captureLoggerOutput(() -> function.invokeProcess(escalation, null));

		// Ensure escalation handled by OfficeFloor escalation handler
		assertTrue("Incorrect escalation logging:" + escalation.getMessage(), log.contains(escalation.getMessage()));
	}

	/**
	 * Functionality to throw {@link Throwable} for escalation handling.
	 */
	public static class EscalationHandlerWork {

		/**
		 * Exception handled by the {@link Office}.
		 */
		public Exception officeException;

		/**
		 * Task causing an escalation.
		 *
		 * @param escalation
		 *            {@link Escalation}.
		 * @throws Throwable
		 *             {@link Escalation}.
		 */
		public void task(Throwable escalation) throws Throwable {
			throw escalation;
		}

		/**
		 * Provides for the {@link Office} {@link EscalationProcedure}.
		 *
		 * @param exception
		 *            Failure to be handled.
		 */
		public void officeEscalation(Exception exception) {
			this.officeException = exception;
		}
	}

}
